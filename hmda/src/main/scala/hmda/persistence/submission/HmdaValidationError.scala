package hmda.persistence.submission

import java.time.Instant

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.filing.submission._
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.persistence.HmdaTypedPersistentActor
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.ActorMaterializer
import akka.stream.typed.scaladsl.ActorFlow
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import hmda.validation.context.ValidationContext
import hmda.parser.filing.ParserFlow._
import hmda.validation.filing.ValidationFlow._
import HmdaProcessingUtils.{
  readRawData,
  updateSubmissionStatus,
  updateSubmissionReceipt
}
import EditDetailsConverter._
import akka.{Done, NotUsed}
import akka.cluster.sharding.typed.scaladsl.EntityRef
import hmda.messages.submission.EditDetailsCommands.{
  EditDetailsPersistenceCommand,
  PersistEditDetails
}
import hmda.messages.submission.EditDetailsEvents.EditDetailsPersistenceEvent
import hmda.publication.KafkaUtils._
import hmda.messages.pubsub.HmdaTopics._
import hmda.query.HmdaQuery._
import hmda.messages.submission.SubmissionProcessingEvents
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.validation.{MacroValidationError, ValidationError}
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.util.streams.FlowUtils.framing
import hmda.validation.filing.MacroValidationFlow._
import hmda.validation.{AS, EC, HmdaValidated, MAT}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import hmda.util.SourceUtils.count

import scala.collection.immutable

object HmdaValidationError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaValidationErrorState] {

  override val name: String = "HmdaValidationError"

  val config = ConfigFactory.load()
  val futureTimeout = config.getInt("hmda.actor.timeout")
  val processingYear = config.getInt("hmda.filing.year")

  implicit val timeout: Timeout = Timeout(futureTimeout.seconds)

  override def behavior(
      entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      PersistentBehavior[SubmissionProcessingCommand,
                         SubmissionProcessingEvent,
                         HmdaValidationErrorState](
        persistenceId = PersistenceId(s"$entityId"),
        emptyState = HmdaValidationErrorState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).snapshotEvery(1000)
    }

  override def commandHandler(ctx: ActorContext[SubmissionProcessingCommand])
    : CommandHandler[SubmissionProcessingCommand,
                     SubmissionProcessingEvent,
                     HmdaValidationErrorState] = { (state, cmd) =>
    val log = ctx.asScala.log
    implicit val system: ActorSystem = ctx.asScala.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    val sharding = ClusterSharding(ctx.asScala.system)

    cmd match {
      case StartSyntacticalValidity(submissionId) =>
        updateSubmissionStatus(sharding, submissionId, Validating, log)
        log.info(s"Syntactical / Validity validation started for $submissionId")

        val fValidationContext =
          validationContext(processingYear, sharding, ctx, submissionId)

        val fSyntacticalValidity = for {
          validationContext <- fValidationContext
          tsErrors <- validateTs(ctx, submissionId, validationContext)
            .runWith(Sink.ignore)
//          tsLarErrors <- validateTsLar(ctx, submissionId, validationContext)
          larSyntacticalValidityErrors <- validateLar("syntactical-validity",
                                                      ctx,
                                                      submissionId,
                                                      validationContext)
            .runWith(Sink.ignore)
          larAsyncErrors <- validateAsyncLar("syntactical-validity",
                                             ctx,
                                             submissionId).runWith(Sink.ignore)
        } yield
//          (tsErrors, tsLarErrors, larSyntacticalValidityErrors, larAsyncErrors)
        (tsErrors, larSyntacticalValidityErrors, larAsyncErrors)
        fSyntacticalValidity.onComplete {
          case Success(_) =>
            ctx.asScala.self ! CompleteSyntacticalValidity(submissionId)
          case Failure(e) =>
            updateSubmissionStatus(sharding, submissionId, Failed, log)
            log.error(e.getLocalizedMessage)
        }

        Effect.none

      case CompleteSyntacticalValidity(submissionId) =>
        log.info(
          s"Syntactical / Validity validation finished for $submissionId")
        val updatedStatus =
          if (state.syntactical.nonEmpty || state.validity.nonEmpty) {
            SyntacticalOrValidityErrors
          } else {
            SyntacticalOrValidity
          }
        Effect
          .persist(
            SyntacticalValidityCompleted(submissionId, updatedStatus.code))
          .thenRun { _ =>
            updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
          }

      case StartQuality(submissionId) =>
        log.info(s"Quality validation started for $submissionId")

        val fQuality = for {
          larErrors <- validateLar("quality",
                                   ctx,
                                   submissionId,
                                   ValidationContext())
            .runWith(Sink.ignore)
          larAsyncErrorsQuality <- validateAsyncLar("quality",
                                                    ctx,
                                                    submissionId)
            .runWith(Sink.ignore)
        } yield (larErrors, larAsyncErrorsQuality)

        fQuality.onComplete {
          case Success(_) =>
            ctx.asScala.self ! CompleteQuality(submissionId)
          case Failure(e) =>
            updateSubmissionStatus(sharding, submissionId, Failed, log)
            log.error(e.getLocalizedMessage)
        }

        Effect.none

      case CompleteQuality(submissionId) =>
        log.info(s"Quality validation finished for $submissionId")
        val updatedStatus =
          if (state.quality.nonEmpty) {
            QualityErrors
          } else {
            Quality
          }
        Effect
          .persist(QualityCompleted(submissionId, updatedStatus.code))
          .thenRun { _ =>
            updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
          }

      case StartMacro(submissionId) =>
        log.info(s"Macro validation started for $submissionId")

        val fMacroEdits: Future[List[ValidationError]] =
          validateMacro(ctx, submissionId)

        fMacroEdits.onComplete {
          case Success(edits) =>
            edits.foreach { edit =>
              ctx.asScala.self ! PersistMacroError(
                submissionId,
                edit.asInstanceOf[MacroValidationError],
                None)
            }
            if (edits.nonEmpty) {
              updateSubmissionStatus(sharding, submissionId, MacroErrors, log)
            } else if (state.qualityVerified) {
              updateSubmissionStatus(sharding, submissionId, Verified, log)
            } else {
              updateSubmissionStatus(sharding, submissionId, Macro, log)
            }
            ctx.asScala.self ! CompleteMacro(submissionId)
          case Failure(e) =>
            log.error(e.getLocalizedMessage)

        }
        Effect.none

      case CompleteMacro(submissionId) =>
        log.info(s"Macro Validation finished for $submissionId")
        val updatedStatus =
          if (!state.macroVerified) MacroErrors
          else if (state.qualityVerified) Verified
          else Macro
        Effect.persist(MacroCompleted(submissionId, updatedStatus.code))

      case PersistHmdaRowValidatedError(submissionId,
                                        rowNumber,
                                        validationErrors,
                                        maybeReplyTo) =>
        val editDetailPersistence = sharding
          .entityRefFor(EditDetailsPersistence.typeKey,
                        s"${EditDetailsPersistence.name}-$submissionId")

        if (validationErrors.nonEmpty) {
          Effect
            .persist(HmdaRowValidatedError(rowNumber, validationErrors))
            .thenRun { _ =>
              log.debug(
                s"Persisted: ${HmdaRowValidatedError(rowNumber, validationErrors)}")

              val hmdaRowValidatedError =
                HmdaRowValidatedError(rowNumber, validationErrors)

              for {
                _ <- persistEditDetails(editDetailPersistence,
                                        hmdaRowValidatedError)
              } yield {
                maybeReplyTo match {
                  case Some(replyTo) =>
                    replyTo ! hmdaRowValidatedError
                  case None => //Do nothing
                }
              }
            }
        } else {
          Effect.none
        }

      case PersistMacroError(_, validationError, maybeReplyTo) =>
        Effect.persist(HmdaMacroValidatedError(validationError)).thenRun { _ =>
          log.debug(s"Persisted: $validationError")
          maybeReplyTo match {
            case Some(replyTo) =>
              replyTo ! validationError
            case None => //do nothing
          }
        }

      case VerifyQuality(submissionId, verified, replyTo) =>
        if (List(Quality.code, QualityErrors.code, Macro.code, MacroErrors.code)
              .contains(state.statusCode) || !verified) {
          Effect
            .persist(
              QualityVerified(submissionId,
                              verified,
                              SubmissionStatus.valueOf(state.statusCode)))
            .thenRun { validationState =>
              val updatedStatus =
                SubmissionStatus.valueOf(validationState.statusCode)
              updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
              replyTo ! QualityVerified(submissionId, verified, updatedStatus)
            }
        } else {
          replyTo ! NotReadyToBeVerified(submissionId)
          Effect.none
        }

      case VerifyMacro(submissionId, verified, replyTo) =>
        if (List(Macro.code, MacroErrors.code)
              .contains(state.statusCode) || !verified) {
          Effect
            .persist(
              MacroVerified(submissionId,
                            verified,
                            SubmissionStatus.valueOf(state.statusCode)))
            .thenRun { validationState =>
              val updatedStatus =
                SubmissionStatus.valueOf(validationState.statusCode)
              updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
              replyTo ! MacroVerified(submissionId, verified, updatedStatus)
            }
        } else {
          replyTo ! NotReadyToBeVerified(submissionId)
          Effect.none
        }

      case SignSubmission(submissionId, replyTo) =>
        if (state.statusCode == Verified.code) {
          val timestamp = Instant.now().toEpochMilli
          val signed = SubmissionSigned(submissionId, timestamp, Signed)
          if ((state.qualityVerified && state.macroVerified) || state
                .noEditsFound()) {
            Effect.persist(signed).thenRun { _ =>
              log.info(
                s"Submission $submissionId signed at ${Instant.ofEpochMilli(timestamp)}")
              updateSubmissionStatus(sharding, submissionId, Signed, log)
              updateSubmissionReceipt(
                sharding,
                submissionId,
                signed.timestamp,
                s"${signed.submissionId}-${signed.timestamp}",
                log)
              publishSignEvent(submissionId).map(signed =>
                log.info(s"Published signed event for $submissionId"))
              replyTo ! signed
            }
          } else {
            replyTo ! SubmissionNotReadyToBeSigned(submissionId)
            Effect.none
          }
        } else {
          replyTo ! SubmissionNotReadyToBeSigned(submissionId)
          Effect.none
        }

      case GetHmdaValidationErrorState(_, replyTo) =>
        replyTo ! state
        Effect.none

      case _ =>
        Effect.none
    }

  }

  override def eventHandler
    : (HmdaValidationErrorState,
       SubmissionProcessingEvent) => HmdaValidationErrorState = {
    case (state, error @ HmdaRowValidatedError(_, _)) =>
      state.updateErrors(error)
    case (state, error @ HmdaMacroValidatedError(_)) =>
      state.updateMacroErrors(error)
    case (state, SyntacticalValidityCompleted(_, statusCode)) =>
      state.updateStatusCode(statusCode)
    case (state, QualityCompleted(_, statusCode)) =>
      state.updateStatusCode(statusCode)
    case (state, MacroCompleted(_, statusCode)) =>
      state.updateStatusCode(statusCode)
    case (state, evt: QualityVerified) =>
      state.verifyQuality(evt)
    case (state, evt: MacroVerified) =>
      state.verifyMacro(evt)
    case (state, SubmissionSigned(_, _, _)) =>
      state.updateStatusCode(Signed.code)
    case (state, _) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding)
  }

  private def validateTs[as: AS](ctx: ActorContext[SubmissionProcessingCommand],
                                 submissionId: SubmissionId,
                                 validationContext: ValidationContext)
    : Source[HmdaRowValidatedError, NotUsed] = {
    uploadConsumerRawStr(ctx, submissionId)
      .take(1)
      .via(validateTsFlow("all", validationContext))
      .zip(Source.fromIterator(() => Iterator.from(1)))
      .collect {
        case (Left(errors), rowNumber) =>
          PersistHmdaRowValidatedError(submissionId, rowNumber, errors, None)
      }
      .via(
        ActorFlow.ask(ctx.asScala.self)(
          (el, replyTo: ActorRef[HmdaRowValidatedError]) =>
            PersistHmdaRowValidatedError(submissionId,
                                         el.rowNumber,
                                         el.validationErrors,
                                         Some(replyTo))
        ))
  }

  private def validateTsLar[as: AS, mat: MAT, ec: EC](
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId,
      validationContext: ValidationContext): Future[Unit] = {

    val headerResultTest: Future[TransmittalSheet] =
      uploadConsumerRawStr(ctx, submissionId)
        .take(1)
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .map(s => TsCsvParser(s))
        .collect {
          case Right(ts) => ts
        }
        .runWith(Sink.head)

    val restResult: Future[immutable.Seq[LoanApplicationRegister]] =
      uploadConsumerRawStr(ctx, submissionId)
        .drop(1)
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .map(s => LarCsvParser(s))
        .collect {
          case Right(lar) => lar
        }
        .runWith(Sink.seq)

    for {
      header <- headerResultTest
      rest <- restResult
    } yield {
      val tsLar = TransmittalLar(header, rest)
      validateTsLarEdits(tsLar, "all", validationContext) match {
        case Left(errors) => {
          ctx.asScala.self.toUntyped ? PersistHmdaRowValidatedError(
            submissionId,
            1,
            errors,
            None)
        }

      }
    }

  }

  private def validateLar[as: AS](
      editCheck: String,
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId,
      validationContext: ValidationContext)
    : Source[HmdaRowValidatedError, NotUsed] = {
    uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(validateLarFlow(editCheck, validationContext))
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .collect {
        case (Left(errors), rowNumber) =>
          PersistHmdaRowValidatedError(submissionId, rowNumber, errors, None)
      }
      .via(
        ActorFlow.ask(ctx.asScala.self)(
          (el, replyTo: ActorRef[HmdaRowValidatedError]) =>
            PersistHmdaRowValidatedError(submissionId,
                                         el.rowNumber,
                                         el.validationErrors,
                                         Some(replyTo))
        ))
  }

  private def validateMacro[as: AS, mat: MAT, ec: EC](
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId
  ): Future[List[ValidationError]] = {
    val larSource = uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .collect {
        case Right(lar) => lar
      }

    for {
      macroEdits <- macroValidation(larSource)
    } yield {
      macroEdits
    }
  }

  private def validateAsyncLar[as: AS, mat: MAT, ec: EC](
      editCheck: String,
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId
  ) = {
    uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(validateAsyncLarFlow(editCheck))
      .map { x =>
        x.collect {
          case Left(errors) => errors
          case Right(_)     => Nil
        }
      }
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .map { x =>
        x._1
          .map { errors =>
            PersistHmdaRowValidatedError(submissionId, x._2, errors, None)
          }
      }
      .mapAsync(2)(f => f.map(x => ctx.asScala.self.toUntyped ? x))
  }

  private def maybeTs(ctx: ActorContext[SubmissionProcessingCommand],
                      submissionId: SubmissionId)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      ec: ExecutionContext): Future[Option[TransmittalSheet]] = {
    uploadConsumerRawStr(ctx, submissionId)
      .take(1)
      .via(parseTsFlow)
      .map(_.getOrElse(TransmittalSheet()))
      .runWith(Sink.seq)
      .map(xs => xs.headOption)
  }

  private def validationContext[as: AS, mat: MAT, ec: EC](
      year: Int,
      sharding: ClusterSharding,
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId): Future[ValidationContext] = {
    val institutionPersistence =
      sharding.entityRefFor(
        InstitutionPersistence.typeKey,
        s"${InstitutionPersistence.name}-${submissionId.lei}")

    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
        ref => GetInstitution(ref))

    for {
      ts <- maybeTs(ctx, submissionId)
      institution <- fInstitution
    } yield {
      ValidationContext(institution, Some(year), ts)
    }
  }

  private def uploadConsumerRawStr[as: AS](
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId): Source[ByteString, NotUsed] = {
    readRawData(submissionId)
      .map(line => line.data)
      .map(ByteString(_))
  }

  private def persistEditDetails[ec: EC](
      editDetailPersistence: EntityRef[EditDetailsPersistenceCommand],
      hmdaRowValidatedError: HmdaRowValidatedError)
    : Future[Iterable[EditDetailsPersistenceEvent]] = {

    val details = validatedRowToEditDetails(hmdaRowValidatedError)

    val fDetails = details.map { detail =>
      val fDetailEvent
        : Future[EditDetailsPersistenceEvent] = editDetailPersistence ? (ref =>
        PersistEditDetails(detail, Some(ref)))
      fDetailEvent
    }

    Future.sequence(fDetails)

  }

  private def publishSignEvent(submissionId: SubmissionId)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer): Future[Done] = {
    produceRecord(signTopic, submissionId.lei, submissionId.toString)

  }

}
