package hmda.persistence.submission

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Behavior, TypedActorContext}
import akka.actor.{ActorSystem, Scheduler}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.pattern.ask
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl._
import akka.persistence.typed.scaladsl.EventSourcedBehavior._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source, _}
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.{ByteString, Timeout}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory
import hmda.HmdaPlatform
import hmda.messages.institution.InstitutionCommands.{
  GetInstitution,
  ModifyInstitution
}
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.messages.pubsub.HmdaTopics._
import hmda.messages.submission.EditDetailsCommands.{
  EditDetailsPersistenceCommand,
  PersistEditDetails
}
import hmda.messages.submission.EditDetailsEvents.EditDetailsPersistenceEvent
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.filing.submission._
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.model.institution.Institution
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.model.validation.{
  MacroValidationError,
  QualityValidationError,
  SyntacticalValidationError,
  ValidationError
}
import hmda.parser.filing.ParserFlow._
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.HmdaTypedPersistentActor
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.EditDetailsConverter._
import hmda.persistence.submission.HmdaProcessingUtils.{
  readRawData,
  updateSubmissionReceipt,
  updateSubmissionStatus
}
import hmda.publication.KafkaUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.validation.context.ValidationContext
import hmda.validation.filing.MacroValidationFlow._
import hmda.validation.filing.ValidationFlow._
import hmda.validation.rules.lar.quality.common.Q600
import hmda.validation.rules.lar.syntactical.S305
import hmda.validation.{AS, EC, MAT}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object HmdaValidationError
  extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
    SubmissionProcessingEvent,
    HmdaValidationErrorState] {

  override val name: String = "HmdaValidationError"

  val config = ConfigFactory.load()
  val futureTimeout = config.getInt("hmda.actor.timeout")

  implicit val timeout: Timeout = Timeout(futureTimeout.seconds)

  override def behavior(
                         entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior[SubmissionProcessingCommand,
        SubmissionProcessingEvent,
        HmdaValidationErrorState](
        persistenceId = PersistenceId(s"$entityId"),
        emptyState = HmdaValidationErrorState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000,
        keepNSnapshots = 10))
    }

  override def commandHandler(
                               ctx: TypedActorContext[SubmissionProcessingCommand])
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
          validationContext(submissionId.period.toInt,
            sharding,
            ctx,
            submissionId)

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
        val period = Try {
          submissionId.period.toInt
        }.fold(_ => 2018, identity)
        val fQuality = for {

          larErrors <- validateLar(
            "quality",
            ctx,
            submissionId,
            ValidationContext(filingYear = Some(period)))(system,
            materializer,
            blockingEc)
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
            .thenRun { (x: HmdaValidationErrorState) =>
              log.debug(
                s"Persisted: ${HmdaRowValidatedError(rowNumber, validationErrors)}")

              val hmdaRowValidatedError =
                HmdaRowValidatedError(rowNumber, validationErrors)

              persistEditDetails(editDetailPersistence, hmdaRowValidatedError)
                .onComplete {
                  case Success(_) =>
                    maybeReplyTo.foreach(_ ! hmdaRowValidatedError)

                  case Failure(exception) =>
                    log.error(
                      "persistEditDetails failed in HmdaValidationError",
                      exception)
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
              setHmdaFilerFlag(submissionId.lei, submissionId.period, sharding)
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

      case GetVerificationStatus(replyTo) =>
        replyTo ! VerificationStatus(qualityVerified = state.qualityVerified,
          macroVerified = state.macroVerified)
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

  private def validateTs[as: AS](
                                  ctx: TypedActorContext[SubmissionProcessingCommand],
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
                                                       ctx: TypedActorContext[SubmissionProcessingCommand],
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

    case class DistinctElementsResult(totalCount: Int,
                                      duplicateLineNumbers: Vector[Int],
                                      checkType: DistinctCheckType)
    sealed trait DistinctCheckType
    case object RawLine extends DistinctCheckType
    case object ULI extends DistinctCheckType

    def checkForDistinctElements(
                                  checkType: DistinctCheckType): Future[DistinctElementsResult] = {
      uploadConsumerRawStr(ctx, submissionId)
        .drop(1) // header
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .zip(Source.fromIterator(() => Iterator.from(2))) // rows start from #1 but we dropped the header line so we start at #2
        .map {
          case (line, rowNumber) => (LarCsvParser(line), line, rowNumber)
        }
        .collect {
          case (Right(parsed), line, rowNumber) => (parsed, line, rowNumber)
        }
        .mapAsync(1) {
          case (lar, rawLine, rowNumber) =>
            checkType match {
              case RawLine =>
                // persistsIfNotExists returns true when a record is persisted
                // used for syntactical validity checks
                appDb.distinctCountRepository
                  .persistsIfNotExists(submissionId.toString,
                    md5HashString(rawLine),
                    260.minutes)
                  .map(persisted => (persisted, rowNumber))

              case ULI =>
                // used for quality checks
                appDb.distinctCountRepository
                  .persistsIfNotExists(submissionId.toString + "uli",
                    md5HashString(lar.loan.ULI.toUpperCase),
                    260.minutes)
                  .map(persisted => (persisted, rowNumber))
            }
        }
        .toMat(Sink.fold(
          DistinctElementsResult(totalCount = 0,
            duplicateLineNumbers = Vector.empty,
            checkType)) {
          // duplicate
          case (acc, (persisted, rowNumber)) if !persisted =>
            acc.copy(acc.totalCount + 1, acc.duplicateLineNumbers :+ rowNumber)
          // no duplicate
          case (acc, _) => acc.copy(acc.totalCount + 1)
        })(Keep.right)
        .named(s"checkForDistinctElements[$checkType]-" + submissionId)
        .run()
    }

    def validateAndPersistErrors(
                                  tsLar: TransmittalLar,
                                  checkType: String,
                                  vc: ValidationContext): Future[List[ValidationError]] = {

      // see addTsFieldInformation in ValidationFlow which does something similar
      def enrichErrorInformation(
                                  tsLar: TransmittalLar,
                                  validationError: ValidationError): ValidationError = {
        val s305 = S305.name
        val q600 = Q600.name
        validationError match {
          case s @ SyntacticalValidationError(_, `s305`, _, fields) =>
            s.copyWithFields(
              fields + ("The following row numbers occur multiple times" -> tsLar.duplicateLineNumbers
                .mkString(start = "Rows: ", sep = ",", end = "")))

          case q @ QualityValidationError(uli, `q600`, fields) =>
            q.copyWithFields(
              fields + ("The following row numbers have the same ULI" -> tsLar.duplicateLineNumbers
                .mkString(start = "Rows: ", sep = ",", end = "")))

          case rest =>
            rest
        }
      }

      log.info(
        s"ValidateTsLar counts ${checkType} - ${submissionId}: TS Total Lines ${tsLar.ts.totalLines} Lars Count ${tsLar.larsCount} ${tsLar.larsDistinctCount} Distinct ULI Count ${tsLar.distinctUliCount}")
      validateTsLarEdits(tsLar, checkType, validationContext) match {

        case Left(errors: Seq[ValidationError]) =>
          val enrichedErrors = errors.map(enrichErrorInformation(tsLar, _))
          val persisted: Future[HmdaRowValidatedError] = ctx.asScala.self ? (
            (ref: ActorRef[HmdaRowValidatedError]) =>
              PersistHmdaRowValidatedError(submissionId,
                1,
                enrichedErrors,
                Some(ref)))
          persisted.map(_ => enrichedErrors)

        case Right(_) =>
          Future.successful(Nil)
      }
    }

    editType match {
      case "syntactical-validity" =>
        for {
          header <- headerResultTest
          distinctResult <- checkForDistinctElements(RawLine)
          distinctCount <- appDb.distinctCountRepository.count(
            submissionId.toString) //S304 and //S305
          res <- validateAndPersistErrors(
            TransmittalLar(
              ts = header,
              larsCount = distinctResult.totalCount,
              larsDistinctCount = distinctCount,
              distinctUliCount = -1,
              duplicateLineNumbers = distinctResult.duplicateLineNumbers.toList,
            ),
            editType,
            validationContext
          )
        } yield res
      case "quality" =>
        for {
          header <- headerResultTest
          distinctResult <- checkForDistinctElements(ULI)
          distinctUliCount <- appDb.distinctCountRepository.count(
            submissionId.toString + "uli") //Q600
          _ <- appDb.distinctCountRepository.remove(submissionId.toString)
          _ <- appDb.distinctCountRepository.remove(
            submissionId.toString + "uli")
          res <- validateAndPersistErrors(
            TransmittalLar(ts = header,
              larsCount = -1,
              larsDistinctCount = -1,
              distinctUliCount = distinctUliCount,
              duplicateLineNumbers =
                distinctResult.duplicateLineNumbers.toList),
            editType,
            validationContext
          )
        } yield res
    }
  }

  private def validateLar(editCheck: String,
                          ctx: TypedActorContext[SubmissionProcessingCommand],
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
        .zip(Source.fromIterator(() => Iterator.from(2))) // rows start from #1 but we dropped the header line so we start at #2
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
                                                       ctx: TypedActorContext[SubmissionProcessingCommand],
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
    val tsSource = uploadConsumerRawStr(ctx, submissionId)
      .take(1)
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => TsCsvParser(s))
      .collect {
        case Right(ts) => ts
      }

    for {
      macroEdits <- macroValidation(larSource, tsSource, submissionId)
    } yield {
      macroEdits
    }
  }

  private def validateAsyncLar[as: AS, mat: MAT, ec: EC](
                                                          editCheck: String,
                                                          ctx: TypedActorContext[SubmissionProcessingCommand],
                                                          submissionId: SubmissionId
                                                        ): Source[HmdaRowValidatedError, NotUsed] = {
    val year = submissionId.period.toInt
    uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(validateAsyncLarFlow(editCheck, year))
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

  private def maybeTs(ctx: TypedActorContext[SubmissionProcessingCommand],
                      submissionId: SubmissionId)(
                       implicit system: ActorSystem,
                       materializer: ActorMaterializer,
                       ec: ExecutionContext): Future[Option[TransmittalSheet]] = {
    uploadConsumerRawStr(ctx, submissionId)
      .take(1)
      .via(parseTsFlow)
      .map(_._1)
      .map(_.getOrElse(TransmittalSheet()))
      .named("maybeTs" + submissionId)
      .runWith(Sink.seq)
      .map(xs => xs.headOption)
  }

  private def validationContext[as: AS, mat: MAT, ec: EC](
                                                           year: Int,
                                                           sharding: ClusterSharding,
                                                           ctx: TypedActorContext[SubmissionProcessingCommand],
                                                           submissionId: SubmissionId): Future[ValidationContext] = {
    val institutionPersistence = {
      if (year == 2018) {
        sharding.entityRefFor(
          InstitutionPersistence.typeKey,
          s"${InstitutionPersistence.name}-${submissionId.lei}")
      } else {
        sharding.entityRefFor(
          InstitutionPersistence.typeKey,
          s"${InstitutionPersistence.name}-${submissionId.lei}-$year")
      }
    }

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
                                            ctx: TypedActorContext[SubmissionProcessingCommand],
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
                                                          period: String,
                                                          sharding: ClusterSharding): Unit = {

    val institutionPersistence =
      if (period == "2018") {
        sharding.entityRefFor(InstitutionPersistence.typeKey,
          s"${InstitutionPersistence.name}-$institutionID")
      } else {
        sharding.entityRefFor(
          InstitutionPersistence.typeKey,
          s"${InstitutionPersistence.name}-$institutionID-$period")
      }

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
