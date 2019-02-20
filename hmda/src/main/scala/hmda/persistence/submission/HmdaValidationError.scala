package hmda.persistence.submission

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.actor.{ActorSystem, Scheduler}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.pattern.ask
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source, _}
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.{ByteString, Timeout}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory
import hmda.HmdaPlatform
import hmda.messages.institution.InstitutionCommands.{GetInstitution, ModifyInstitution}
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.messages.pubsub.HmdaTopics._
import hmda.messages.submission.EditDetailsCommands.{EditDetailsPersistenceCommand, PersistEditDetails}
import hmda.messages.submission.EditDetailsEvents.EditDetailsPersistenceEvent
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.filing.submission._
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.model.institution.Institution
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.model.validation.{MacroValidationError, ValidationError}
import hmda.parser.filing.ParserFlow._
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.HmdaTypedPersistentActor
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.EditDetailsConverter._
import hmda.persistence.submission.HmdaProcessingUtils.{readRawData, updateSubmissionReceipt, updateSubmissionStatus}
import hmda.publication.KafkaUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.validation.context.ValidationContext
import hmda.validation.filing.MacroValidationFlow._
import hmda.validation.filing.ValidationFlow._
import hmda.validation.{AS, EC, MAT}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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
    val blockingEc: ExecutionContext =
      system.dispatchers.lookup("akka.blocking-quality-dispatcher")
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
            .toMat(Sink.ignore)(Keep.right)
            .named("validateTs[Syntactical]-" + submissionId)
            .run()

          tsLarErrors <- validateTsLar(ctx,
                                       submissionId,
                                       "syntactical-validity",
                                       validationContext)
          _ = log.info(s"Starting validateLar - Syntactical for $submissionId")
          larSyntacticalValidityErrors <- validateLar(
            "syntactical-validity",
            ctx,
            submissionId,
            validationContext)(system, materializer, blockingEc)
          _ = log.info(
            s"Starting validateAsycLar - Syntactical for $submissionId")
          larAsyncErrors <- validateAsyncLar("syntactical-validity",
                                             ctx,
                                             submissionId).runWith(Sink.ignore)
          _ = log.info(
            s"Finished validateAsycLar - Syntactical for $submissionId")
        } yield
          (tsErrors, tsLarErrors, larSyntacticalValidityErrors, larAsyncErrors)
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

          larErrors <- validateLar(
            "quality",
            ctx,
            submissionId,
            ValidationContext())(system, materializer, blockingEc)
          _ = log.info(s"Finished ValidateLar Quality for $submissionId")
          _ = log.info(s"Started validateAsyncLar - Quality for $submissionId")
          larAsyncErrorsQuality <- validateAsyncLar("quality",
                                                    ctx,
                                                    submissionId)
            .runWith(Sink.ignore)
          _ = log.info(s"Finished ValidateAsyncLar Quality for $submissionId")
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
            val persistedEdits = Future.sequence(edits.map { edit =>
              ctx.asScala.self.toUntyped ? PersistMacroError(
                submissionId,
                edit.asInstanceOf[MacroValidationError],
                None)
            })
            persistedEdits.onComplete(_ => {
              ctx.asScala.self ! CompleteMacro(submissionId)
            })
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
        updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
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
              setHmdaFilerFlag(submissionId.lei, sharding)
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
      editType: String,
      validationContext: ValidationContext): Future[List[ValidationError]] = {
    implicit val scheduler: Scheduler = ctx.asScala.system.scheduler
    val log = ctx.asScala.log
    val appDb = HmdaPlatform.appDb

    log.info(s"Starting validateTsLar for $submissionId")

    def md5HashString(s: String): String = {
      val md = MessageDigest.getInstance("MD5")
      val digest = md.digest(s.getBytes)
      val bigInt = new BigInteger(1, digest)
      val hashedString = bigInt.toString(16)
      hashedString
    }

    def headerResultTest: Future[TransmittalSheet] =
      uploadConsumerRawStr(ctx, submissionId)
        .take(1)
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .map(s => TsCsvParser(s))
        .collect {
          case Right(ts) => ts
        }
        .toMat(Sink.head)(Keep.right)
        .named("headerResult[Syntactical]-" + submissionId)
        .run()

    def persistElements: Future[Int] =
      uploadConsumerRawStr(ctx, submissionId)
        .drop(1) // header
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .map(line => (LarCsvParser(line), line))
        .collect {
          case (Right(parsed), line) => (parsed, line)
        }
        .map { x =>
          (md5HashString(x._1.loan.ULI.toUpperCase),
           md5HashString(x._2.toUpperCase))
        }
        .mapAsync(1) { x =>
          for {
            line <- appDb.distinctCountRepository.persist(submissionId.toString,
                                                          x._2,
                                                          260.minutes)
            uli <- appDb.distinctCountRepository.persist(
              submissionId.toString + "uli",
              x._1,
              260.minutes)
          } yield (line)
        }
        .toMat(Sink.fold(0)((acc, _) => acc + 1))(Keep.right)
        .named("persistElements[Syntactical]-" + submissionId)
        .run()

    def validateAndPersistErrors(
        tsLar: TransmittalLar,
        checkType: String,
        vc: ValidationContext): Future[List[ValidationError]] = {
      log.info(
        s"ValidateTsLar counts ${checkType} - ${submissionId}: TS Total Lines ${tsLar.ts.totalLines} Lars Count ${tsLar.larsCount} ${tsLar.larsDistinctCount} Distinct ULI Count ${tsLar.distinctUliCount}")
      validateTsLarEdits(tsLar, checkType, validationContext) match {

        case Left(errors: Seq[ValidationError]) =>
          val persisted: Future[HmdaRowValidatedError] = ctx.asScala.self ? (
              (ref: ActorRef[HmdaRowValidatedError]) =>
                PersistHmdaRowValidatedError(submissionId,
                                             1,
                                             errors,
                                             Some(ref)))
          persisted.map(_ => errors)

        case Right(_) =>
          Future.successful(Nil)
      }
    }

    editType match {
      case "syntactical-validity" =>
        for {
          header <- headerResultTest
          count <- persistElements
          distinctCount <- appDb.distinctCountRepository.count(
            submissionId.toString) //S304 and //S305
          res <- validateAndPersistErrors(
            TransmittalLar(header, count, distinctCount, -1),
            editType,
            validationContext)
        } yield res
      case "quality" =>
        for {
          header <- headerResultTest
          distinctUliCount <- appDb.distinctCountRepository.count(
            submissionId.toString + "uli") //Q600
          _ <- appDb.distinctCountRepository.remove(submissionId.toString)
          _ <- appDb.distinctCountRepository.remove(
            submissionId.toString + "uli")
          res <- validateAndPersistErrors(
            TransmittalLar(header, -1, -1, distinctUliCount),
            editType,
            validationContext)
        } yield res
    }
  }

  private def validateLar(editCheck: String,
                          ctx: ActorContext[SubmissionProcessingCommand],
                          submissionId: SubmissionId,
                          validationContext: ValidationContext)(
      implicit actorSystem: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContext): Future[Unit] = {

    def qualityChecks: Future[List[ValidationError]] =
      if (editCheck == "quality") {
        validateTsLar(ctx, submissionId, "quality", validationContext)
      } else {
        Future.successful(Nil)
      }

    def errorPersisting: Future[Done] =
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
        .named("errorPersisting" + submissionId)
        .runWith(Sink.ignore)

    for {
      quality <- qualityChecks
      qualityErrors <- errorPersisting
    } yield ()
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
  ): Source[HmdaRowValidatedError, NotUsed] = {
    uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(validateAsyncLarFlow(editCheck))
      .filter(_.isLeft)
      .map(_.left.get)
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .via(ActorFlow.ask(ctx.asScala.self) {
        case ((message: Seq[ValidationError], index: Int),
              replyTo: ActorRef[HmdaRowValidatedError]) =>
          PersistHmdaRowValidatedError(submissionId,
                                       index,
                                       message,
                                       Some(replyTo))
      })
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
      .named("maybeTs" + submissionId)
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

  private def setHmdaFilerFlag[as: AS, mat: MAT, ec: EC](
      institutionID: String,
      sharding: ClusterSharding): Unit = {

    val institutionPersistence =
      sharding.entityRefFor(InstitutionPersistence.typeKey,
                            s"${InstitutionPersistence.name}-$institutionID")

    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
        ref => GetInstitution(ref))

    for {
      maybeInst <- fInstitution
    } yield {
      val institution = maybeInst.getOrElse(Institution.empty)
      val modifiedInstitution = institution.copy(hmdaFiler = true)
      if (institution.LEI.nonEmpty) {
        val modified: Future[InstitutionEvent] =
          institutionPersistence ? (ref =>
            ModifyInstitution(modifiedInstitution, ref))
        modified
      }
    }
  }

}
