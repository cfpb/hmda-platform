package hmda.persistence.submission

import java.time.Instant
import akka.actor.typed._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.pattern.ask
import akka.persistence.typed.scaladsl.EventSourcedBehavior.{CommandHandler, EventHandler}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, _}
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.{ByteString, Timeout}
import akka.{Done, NotUsed}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import hmda.HmdaPlatform.stringKafkaProducer
import hmda.messages.institution.InstitutionCommands.{GetInstitution, ModifyInstitution}
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.messages.pubsub.HmdaTopics._
import hmda.messages.submission.EditDetailsCommands.{EditDetailsPersistenceCommand, PersistEditDetails}
import hmda.messages.submission.EditDetailsEvents.EditDetailsPersistenceEvent
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.messages.submission.ValidationProgressTrackerCommands._
import hmda.model.filing.submission._
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.model.institution.Institution
import hmda.model.processing.state.ValidationProgress.InProgress
import hmda.model.processing.state.{HmdaValidationErrorState, ValidationProgress, ValidationType}
import hmda.model.validation.{MacroValidationError, QualityValidationError, SyntacticalValidationError, ValidationError}
import hmda.parser.filing.ParserFlow._
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.HmdaTypedPersistentActor
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.EditDetailsConverter._
import hmda.persistence.submission.HmdaProcessingUtils.{readRawData, updateSubmissionStatus, updateSubmissionStatusAndReceipt}
import hmda.publication.KafkaUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period
import hmda.validation.aggregate.DistinctElements
import hmda.validation.context.ValidationContext
import hmda.validation.filing.MacroValidationFlow._
import hmda.validation.filing.ValidationFlow._
import hmda.validation.rules.lar.quality._2020.Q600_warning
import hmda.validation.rules.lar.quality.common.Q600
import hmda.validation.rules.lar.syntactical.{S304, S305, S306}

import scala.collection.immutable.ListMap
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
// $COVERAGE-OFF$
object HmdaValidationError
  extends HmdaTypedPersistentActor[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaValidationErrorState] with StrictLogging {

  override val name: String = "HmdaValidationError"

  val quarterlyRegexQ1 = "\\b[0-9]{4}\\b-(Q*[1])$".r
  val quarterlyRegexQ2 = "\\b[0-9]{4}\\b-(Q*[2])$".r
  val quarterlyRegexQ3 = "\\b[0-9]{4}\\b-(Q*[3])$".r

  override def behavior(entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      // Initialization is done like this because of inheritance
      val tracker = ctx.spawn(ValidationProgressTracker(HmdaValidationErrorState()), s"tracker-${ctx.self.path.name}")

      EventSourcedBehavior[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaValidationErrorState](
        persistenceId = PersistenceId.ofUniqueId(entityId),
        emptyState = HmdaValidationErrorState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10)).receiveSignal {
        case (state, RecoveryCompleted) =>
          // send a snapshot of the internal state to the tracker once the actor has completely hydrated from the journal
          tracker ! StateSnapshot(state)

        case (_, PostStop) =>
          // tie the lifecycle of the tracker to the lifecycle of this actor
          ctx.stop(tracker)
      }
    }

  override def commandHandler(
                               ctx: ActorContext[SubmissionProcessingCommand]
                             ): CommandHandler[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaValidationErrorState] = { (state, cmd) =>
    val log                                   = ctx.log
    implicit val system: ActorSystem[_]       = ctx.system
    implicit val blockingEc: ExecutionContext = system.dispatchers.lookup(DispatcherSelector.fromConfig("akka.blocking-quality-dispatcher"))
    val materializer = implicitly[Materializer]

    val config                    = system.settings.config
    val futureTimeout             = config.getInt("hmda.actor.timeout")
    implicit val timeout: Timeout = Timeout(futureTimeout.seconds)
    val sharding                  = ClusterSharding(ctx.system)
    // Look up is done like this because of inheritance (would have ideally passed it into commandHandler)
    val tracker: ActorRef[ValidationProgressTrackerCommand] =
      ctx.child(s"tracker-${ctx.self.path.name}").get.toClassic.toTyped[ValidationProgressTrackerCommand]

    // so It works even if current actor is passivated
    def getSelf(subId: SubmissionId): EntityRef[SubmissionProcessingCommand] = selectHmdaValidationError(sharding, subId)

    cmd match {
      case TrackProgress(replyTo) =>
        Effect.reply(replyTo)(tracker)

      case StartSyntacticalValidity(submissionId) =>
        updateSubmissionStatus(sharding, submissionId, Validating, log)
        log.info(s"Syntactical / Validity validation started for $submissionId")

        val period = submissionId.period

        val fValidationContext =
          validationContext(period, sharding, submissionId)

        tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(1), Set())

        val fSyntacticalValidity = for {
          validationContext <- fValidationContext
          _                 = tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(25), Set())
          tsErrors <- validateTs(ctx, submissionId, validationContext)
            .mapConcat(_.validationErrors)
            .toMat(Sink.seq)(Keep.right)
            .named("validateTs[Syntactical]-" + submissionId)
            .run()
          _           = tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(50), tsErrors.map(_.editName).toSet)
          tsLarErrors <- validateTsLar(submissionId, "syntactical-validity", validationContext, getSelf(submissionId))
          _           = tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(90), tsLarErrors.map(_.editName).toSet)
          _           = log.info(s"Starting validateLar - Syntactical for $submissionId")
          larSyntacticalValidityErrors <- validateLar("syntactical-validity", ctx, submissionId, validationContext)(
            system,
            materializer,
            blockingEc,
            timeout
          )
          _              = tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(95), larSyntacticalValidityErrors.map(_.editName).toSet)
          _              = log.info(s"Starting validateAsycLar - Syntactical for $submissionId")
          larAsyncErrors <- validateAsyncLar("syntactical-validity", submissionId, validationContext).mapConcat(_.validationErrors).runWith(Sink.seq)
          _              = tracker ! ValidationDelta(ValidationType.Syntactical, InProgress(99), larAsyncErrors.map(_.editName).toSet)
          _              = log.info(s"Finished validateAsycLar - Syntactical for $submissionId")
        } yield (tsErrors, tsLarErrors, larSyntacticalValidityErrors, larAsyncErrors)

        fSyntacticalValidity.onComplete {
          case Success(_) =>
            getSelf(submissionId) ! CompleteSyntacticalValidity(submissionId)
          case Failure(e) =>
            updateSubmissionStatus(sharding, submissionId, Failed, log)
            log.error(e.getLocalizedMessage)
        }

        Effect.none

      case CompleteSyntacticalValidity(submissionId) =>
        log.info(s"Syntactical / Validity validation finished for $submissionId")
        val updatedStatus =
          if (state.syntactical.nonEmpty || state.validity.nonEmpty) {
            SyntacticalOrValidityErrors
          } else {
            SyntacticalOrValidity
          }
        Effect
          .persist(SyntacticalValidityCompleted(submissionId, updatedStatus.code))
          .thenRun { _ =>
            updateSubmissionStatus(sharding, submissionId, updatedStatus, log)

            // Update tracker status
            val syntactical = ValidationType.Syntactical
            if (updatedStatus == SyntacticalOrValidity) tracker ! ValidationDelta(syntactical, ValidationProgress.Completed, Set())
            else tracker ! ValidationDelta(syntactical, ValidationProgress.CompletedWithErrors, Set())
          }

      case StartQuality(submissionId) =>
        log.info(s"Quality validation started for $submissionId")
        val period = submissionId.period
        val fValidationContext =
          validationContext(period, sharding, submissionId)
        tracker ! ValidationDelta(ValidationType.Quality, InProgress(1), Set())
        val fQuality = for {
          validationContext <- fValidationContext
          larErrors <- validateLar("quality", ctx, submissionId, validationContext)(
            system,
            materializer,
            blockingEc,
            timeout
          )
          _ = tracker ! ValidationDelta(ValidationType.Quality, InProgress(50), larErrors.map(_.editName).toSet)
          _ = log.info(s"Finished ValidateLar Quality for $submissionId")
          _ = log.info(s"Started validateAsyncLar - Quality for $submissionId")
          larAsyncErrorsQuality <- validateAsyncLar("quality", submissionId, validationContext)
            .mapConcat(_.validationErrors)
            .runWith(Sink.seq)
          _ = log.info(s"Finished ValidateAsyncLar Quality for $submissionId")
          _ = tracker ! ValidationDelta(ValidationType.Quality, InProgress(99), larAsyncErrorsQuality.map(_.editName).toSet)
        } yield (larErrors, larAsyncErrorsQuality)

        fQuality.onComplete {
          case Success(_) =>
            getSelf(submissionId) ! CompleteQuality(submissionId)
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

            // Update tracker status
            val quality = ValidationType.Quality
            if (updatedStatus == Quality) tracker ! ValidationDelta(quality, ValidationProgress.Completed, Set())
            else tracker ! ValidationDelta(quality, ValidationProgress.CompletedWithErrors, Set())
          }

      case StartMacro(submissionId) =>
        log.info(s"Macro validation started for $submissionId")
        tracker ! ValidationDelta(ValidationType.Macro, InProgress(1), Set())
        val fMacroEdits: Future[List[ValidationError]] =
          validateMacro(ctx, submissionId)

        fMacroEdits.onComplete {
          case Success(edits) =>
            val persistedEdits = Future.sequence(edits.map { edit =>
              ctx.self.toClassic ? PersistMacroError(submissionId, edit.asInstanceOf[MacroValidationError], None)
            })
            persistedEdits.onComplete(_ => ctx.self ! CompleteMacro(submissionId))
            tracker ! ValidationDelta(ValidationType.Macro, InProgress(99), edits.map(_.editName).toSet)
          case Failure(e) =>
            log.error(e.getLocalizedMessage)

        }
        Effect.none

      case CompleteMacro(submissionId) =>
        log.info(s"Macro Validation finished for $submissionId")
        val updatedStatus =
          if (state.quality.isEmpty && state.`macro`.isEmpty && !state.macroVerified && !state.qualityVerified)
            Verified //This is for when a submission doesn't contain any quality or macro errors
          else if (!state.macroVerified) MacroErrors
          else if (state.qualityVerified) Verified
          else Macro

        Effect
          .persist(MacroCompleted(submissionId, updatedStatus.code))
          .thenRun { _ =>
            updateSubmissionStatus(sharding, submissionId, updatedStatus, log)

            // Update tracker status
            val macroStatus = ValidationType.Macro
            if (updatedStatus == Verified || updatedStatus == Macro) tracker ! ValidationDelta(macroStatus, ValidationProgress.Completed, Set())
            else tracker ! ValidationDelta(macroStatus, ValidationProgress.CompletedWithErrors, Set())
          }

      case PersistHmdaRowValidatedError(submissionId, rowNumber, validationErrors, maybeReplyTo) =>
        val editDetailPersistence = sharding
          .entityRefFor(EditDetailsPersistence.typeKey, s"${EditDetailsPersistence.name}-$submissionId")

        if (validationErrors.nonEmpty) {
          Effect
            .persist(HmdaRowValidatedError(rowNumber, validationErrors))
            .thenRun { _ =>
              log.debug(s"Persisted: ${HmdaRowValidatedError(rowNumber, validationErrors)}")

              val hmdaRowValidatedError =
                HmdaRowValidatedError(rowNumber, validationErrors)

              persistEditDetails(editDetailPersistence, hmdaRowValidatedError).map { _ =>
                maybeReplyTo match {
                  case Some(replyTo) =>
                    replyTo ! hmdaRowValidatedError

                  case None => //Do nothing
                }
              }
            }
        } else Effect.none

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
            .persist(QualityVerified(submissionId, verified, SubmissionStatus.valueOf(state.statusCode)))
            .thenRun { validationState =>
              val updatedStatus =
                SubmissionStatus.valueOf(validationState.statusCode)
              updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
              replyTo ! QualityVerified(submissionId, verified, updatedStatus)
              log.info(s"Verified Quality for $submissionId")
            }
        } else {
          Effect.reply(replyTo)(NotReadyToBeVerified(submissionId))
        }

      case VerifyMacro(submissionId, verified, replyTo) =>
        if (List(Macro.code, MacroErrors.code)
          .contains(state.statusCode) || !verified) {
          Effect
            .persist(MacroVerified(submissionId, verified, SubmissionStatus.valueOf(state.statusCode)))
            .thenRun { validationState =>
              val updatedStatus =
                SubmissionStatus.valueOf(validationState.statusCode)
              updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
              replyTo ! MacroVerified(submissionId, verified, updatedStatus)
              log.info(s"Verified Macro for $submissionId")
            }
        } else {
          Effect.reply(replyTo)(NotReadyToBeVerified(submissionId))
        }

      case SignSubmission(submissionId, replyTo, email, signerUsername) =>
        if (state.statusCode == Verified.code) {
          val timestamp = Instant.now().toEpochMilli
          val signed    = SubmissionSigned(submissionId, timestamp, Signed)
          val currentNamespace = config.getString("hmda.currentNamespace")
          if (currentNamespace != "default") { //signing the submission is not allowed on Beta namespace
            Effect.reply(replyTo)(SubmissionNotReadyToBeSigned(submissionId))
          }
          else if ((state.qualityVerified && state.macroVerified) || state
            .noEditsFound() || (state.qualityVerified && state.`macro`.isEmpty) || (state.quality.isEmpty && state.macroVerified)) {
            Effect.persist(signed).thenRun { _ =>
              log.info(s"Submission $submissionId signed at ${Instant.ofEpochMilli(timestamp)}")
              updateSubmissionStatusAndReceipt(
                sharding,
                submissionId,
                signed.timestamp,
                s"${signed.submissionId}-${signed.timestamp}",
                Signed,
                log,
                signerUsername
              )

              publishSignEvent(submissionId, email, signed.timestamp, config).map(signed =>
                log.info(
                  s"Published signed event for $submissionId. " +
                    s"${signTopic} (key: ${submissionId.lei}, value: ${submissionId.toString}. " +
                    s"${emailTopic} (key: ${submissionId.toString}, value: ${email})"
                )
              )
              setHmdaFilerFlag(submissionId.lei, submissionId.period, sharding)
              replyTo ! signed
            }
          } else {
            Effect.reply(replyTo)(SubmissionNotReadyToBeSigned(submissionId))
          }
        } else {
          Effect.reply(replyTo)(SubmissionNotReadyToBeSigned(submissionId))
        }

      case GetHmdaValidationErrorState(_, replyTo) =>
        Effect.reply(replyTo)(state)

      case GetVerificationStatus(replyTo) =>
        Effect.reply(replyTo)(VerificationStatus(qualityVerified = state.qualityVerified, macroVerified = state.macroVerified))

      case HmdaParserStop =>
        log.info(s"Stopping ${ctx.self.path.name}")
        Effect.stop()

      case _ =>
        Effect.none
    }
  }

  def eventHandler: EventHandler[HmdaValidationErrorState, SubmissionProcessingEvent] = {
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

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] =
    super.startShardRegion(sharding, HmdaParserStop)

  private def validateTs(
                          ctx: ActorContext[SubmissionProcessingCommand],
                          submissionId: SubmissionId,
                          validationContext: ValidationContext
                        )(implicit t: Timeout, system: ActorSystem[_]): Source[HmdaRowValidatedError, NotUsed] =
    uploadConsumerRawStr(submissionId)
      .take(1)
      .via(validateTsFlow("all", validationContext))
      .zip(Source.fromIterator(() => Iterator.from(1)))
      .collect {
        case (Left(errors), rowNumber) =>
          PersistHmdaRowValidatedError(submissionId, rowNumber, errors, None)
      }
      .via(
        ActorFlow.ask(ctx.self)((el, replyTo: ActorRef[HmdaRowValidatedError]) =>
          PersistHmdaRowValidatedError(submissionId, el.rowNumber, el.validationErrors, Some(replyTo))
        )
      )

  private def validateTsLar(submissionId: SubmissionId,
                            editType: String,
                            validationContext: ValidationContext,
                            self: EntityRef[SubmissionProcessingCommand]
                           )(implicit actorSystem: ActorSystem[_], ec: ExecutionContext, t: Timeout): Future[List[ValidationError]] = {
    logger.info(s"Starting validateTsLar for $submissionId")

    def headerResultTest: Future[TransmittalSheet] =
      uploadConsumerRawStr(submissionId)
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

    def validateAndPersistErrors(tsLar: TransmittalLar, checkType: String, vc: ValidationContext): Future[List[ValidationError]] = {

      logger.info(s"In validateAnPersistErrors for ${submissionId} for ${checkType}")

      def ulisWithLineNumbers(uliToLineNumbers: Map[String, List[Int]]): String =
        uliToLineNumbers.map { case (uli, lineNumbers) => s"Rows: ${lineNumbers.mkString(",")} with ULI: $uli"}.mkString(",")

      // see addTsFieldInformation in ValidationFlow which does something similar
      def enrichErrorInformation(tsLar: TransmittalLar, q600WarningPresent: Boolean, validationError: ValidationError): ValidationError = {
        val s305 = S305.name
        val s304 = S304.name
        val q600name = Q600.name
        val s306 = S306.name
        validationError match {
          case s306 @ SyntacticalValidationError(_, `s306`, _, fields) => //This is newly added for S306
            s306.copyWithFields(
              ListMap(
                "Universal Loan Identifier (ULI)" -> tsLar.uli,
                "The following row numbers occur multiple times and have the same ULI with action type 1 (Loan Originated)" ->
                  ulisWithLineNumbers(tsLar.duplicateUliToLineNumbersUliActionType)
              )).copy(uli=tsLar.ts.LEI)
          case s305 @ SyntacticalValidationError(_, `s305`, _, fields) =>
            s305.copyWithFields(
              fields + ("The following ULIs at the given row numbers occur multiple times" ->
                ulisWithLineNumbers(tsLar.duplicateUliToLineNumbers))
            )
          case s304 @ SyntacticalValidationError(_, `s304`, _, fields) =>
            s304.copyWithFields(
              ListMap(
                "Entries Reported in Transmittal Sheet" -> tsLar.ts.totalLines.toString,
                "Total Number of LARs Found in File"    -> tsLar.larsCount.toString
              )
            )
          case q600 @ QualityValidationError(uli,`q600name`, fields)  =>
            if (q600WarningPresent) {
              q600.copyWithFields(
                fields + (s"The following row numbers have the same ULI. WARNING: Additionally there are rows in your data that have a duplicate ULI, LEI, Action Taken, and Action Taken Date. This edit logic will be changed in 2021 and become a Syntactical edit, unable to be bypassed until corrected." ->
                  ulisWithLineNumbers(tsLar.duplicateUliToLineNumbers))
              )
            } else {
              q600.copyWithFields(
                fields + (s"The following row numbers have the same ULI" ->
                  ulisWithLineNumbers(tsLar.duplicateUliToLineNumbers))
              )
            }

          case rest =>
            rest
        }
      }

      logger.info(
        s"ValidateTsLar counts ${checkType} - ${submissionId}: TS Total Lines ${tsLar.ts.totalLines} Lars Count ${tsLar.larsCount} ${tsLar.larsDistinctCount} Distinct ULI Count ${tsLar.distinctUliCount} Unique LAR Count ${tsLar.uniqueLarsSpecificFields}"
      )
      validateTsLarEdits(tsLar, checkType, validationContext) match {

        case Left(errors: Seq[ValidationError]) =>
          val q600WarningPresent = errors.exists(_.editName == Q600_warning.name)
          val enrichedErrors = errors.map(enrichErrorInformation(tsLar, q600WarningPresent, _))
          val persisted: Future[HmdaRowValidatedError] =
            self ? ((ref: ActorRef[HmdaRowValidatedError]) => PersistHmdaRowValidatedError(submissionId, 1, enrichedErrors, Some(ref)))
          persisted.map(_ => enrichedErrors)

        case Right(_) =>
          Future.successful(Nil)
      }
    }

    editType match {
      case "syntactical-validity" =>
        for {
          header        <- headerResultTest
          rawLineResult <- DistinctElements(DistinctElements.CheckType.RawLine, uploadConsumerRawStr(submissionId), submissionId)
          s306Result    <- DistinctElements(DistinctElements.CheckType.ULIActionTaken, uploadConsumerRawStr(submissionId), submissionId)
          actionTakenWithinRangeResults    <- DistinctElements(DistinctElements.CheckType.ActionTakenWithinRangeCounter, uploadConsumerRawStr(submissionId), submissionId)
          actionTakenGreaterThanRangeResults   <- DistinctElements(DistinctElements.CheckType.ActionTakenGreaterThanRangeCounter, uploadConsumerRawStr(submissionId), submissionId)

          res <- validateAndPersistErrors(
            TransmittalLar(
              ts = header,
              larsCount = rawLineResult.totalCount,
              uli = s306Result.uli,
              larsDistinctCount = rawLineResult.distinctCount,
              uniqueLarsSpecificFields = -1,
              distinctUliCount = -1,
              duplicateUliToLineNumbers = rawLineResult.uliToDuplicateLineNumbers.view.mapValues(_.toList).toMap,
              distinctActionTakenUliCount = s306Result.distinctCount,
              duplicateUliToLineNumbersUliActionType = s306Result.uliToDuplicateLineNumbers.view.mapValues(_.toList).toMap,
              actionTakenDatesWithinRange = actionTakenWithinRangeResults.totalCount,
              actionTakenDatesGreaterThanRange = actionTakenGreaterThanRangeResults.totalCount
            ),
            editType,
            validationContext
          )
        } yield res

      case "quality" =>
        for {
          header    <- headerResultTest
          uliResult <- DistinctElements(DistinctElements.CheckType.ULI, uploadConsumerRawStr(submissionId), submissionId)
          uniqueLarResult <- DistinctElements(DistinctElements.CheckType.UniqueLar, uploadConsumerRawStr(submissionId), submissionId)
          res <- validateAndPersistErrors(
            TransmittalLar(
              ts = header,
              uli = uliResult.uli,
              larsCount = -1,
              larsDistinctCount = -1,
              uniqueLarsSpecificFields = uniqueLarResult.distinctCount,
              distinctUliCount = uliResult.distinctCount,
              duplicateUliToLineNumbers = uliResult.uliToDuplicateLineNumbers.view.mapValues(_.toList).toMap),
            editType,
            validationContext
          )
        } yield res
    }
  }

  private def validateLar(
                           editCheck: String,
                           ctx: ActorContext[SubmissionProcessingCommand],
                           submissionId: SubmissionId,
                           validationContext: ValidationContext
                         )(implicit system: ActorSystem[_], mat: Materializer, ec: ExecutionContext, t: Timeout): Future[List[ValidationError]] = {

    val sharding = ClusterSharding(system)
    val self: EntityRef[SubmissionProcessingCommand] = selectHmdaValidationError(sharding, submissionId)

    def qualityChecks: Future[List[ValidationError]] =
      if (editCheck == "quality") validateTsLar(submissionId, "quality", validationContext, self)
      else Future.successful(Nil)

    def errorPersisting: Future[Seq[ValidationError]] =
      uploadConsumerRawStr(submissionId)
        .drop(1)
        .via(validateLarFlow(editCheck, validationContext))
        .zip(Source.fromIterator(() => Iterator.from(2))) // rows start from #1 but we dropped the header line so we start at #2
        .collect {
          case (Left(errors), rowNumber) =>
            PersistHmdaRowValidatedError(submissionId, rowNumber, errors, None)
        }
        .mapAsync(1)(el =>
          self ? ((replyTo: ActorRef[HmdaRowValidatedError]) => el.copy(replyTo = Some(replyTo)))
        )
        .named("errorPersisting" + submissionId)
        .mapConcat(_.validationErrors)
        .runWith(Sink.seq)

    for {
      qualityErrors <- qualityChecks
      errors <- errorPersisting
    } yield qualityErrors ++ errors.toList
  }

  private def validateMacro(
                             ctx: ActorContext[SubmissionProcessingCommand],
                             submissionId: SubmissionId
                           )(implicit system: ActorSystem[_], ec: ExecutionContext): Future[List[ValidationError]] = {
    val larSource = uploadConsumerRawStr(submissionId)
      .drop(1)
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .collect {
        case Right(lar) => lar
      }
    val tsSource = uploadConsumerRawStr(submissionId)
      .take(1)
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => TsCsvParser(s))
      .collect {
        case Right(ts) => ts
      }

    macroValidation(larSource, tsSource, submissionId)
  }

  private def validateAsyncLar(
                                editCheck: String,
                                submissionId: SubmissionId,
                                validationContext: ValidationContext
                              )(implicit system: ActorSystem[_], mat: Materializer, ec: ExecutionContext, t: Timeout): Source[HmdaRowValidatedError, NotUsed] = {
    val sharding = ClusterSharding(system)
    val self: EntityRef[SubmissionProcessingCommand] = selectHmdaValidationError(sharding, submissionId)

    val period = submissionId.period
    uploadConsumerRawStr(submissionId)
      .drop(1)
      .via(validateAsyncLarFlow(editCheck, period, validationContext))
      .filter(_.isLeft)
      .map(_.left.get)
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .mapAsync(1)(el =>
        self ? ((replyTo: ActorRef[HmdaRowValidatedError]) => PersistHmdaRowValidatedError(submissionId, el._2, el._1, Some(replyTo))
          ))
  }

  private def maybeTs(
                       submissionId: SubmissionId
                     )(implicit system: ActorSystem[_], materializer: Materializer, ec: ExecutionContext): Future[Option[TransmittalSheet]] =
    uploadConsumerRawStr(submissionId)
      .take(1)
      .via(parseTsFlow)
      .map(_._1)
      .map(_.getOrElse(TransmittalSheet()))
      .named("maybeTs" + submissionId)
      .runWith(Sink.seq)
      .map(xs => xs.headOption)

  private def validationContext(
                                 period: Period,
                                 sharding: ClusterSharding,
                                 submissionId: SubmissionId
                               )(implicit as: ActorSystem[_], mat: Materializer, ec: ExecutionContext, t: Timeout): Future[ValidationContext] = {
    val institutionPersistence                    = InstitutionPersistence.selectInstitution(sharding, submissionId.lei, period.year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? GetInstitution
    for {
      ts          <- maybeTs(submissionId)
      institution <- fInstitution
    } yield ValidationContext(institution, Some(period), ts)
  }

  private def uploadConsumerRawStr(
                                    submissionId: SubmissionId
                                  )(implicit as: ActorSystem[_]): Source[ByteString, NotUsed] =
    readRawData(submissionId)
      .map(line => line.data)
      .map(ByteString(_))

  private def persistEditDetails(
                                  editDetailPersistence: EntityRef[EditDetailsPersistenceCommand],
                                  hmdaRowValidatedError: HmdaRowValidatedError
                                )(implicit ec: ExecutionContext, t: Timeout): Future[Iterable[EditDetailsPersistenceEvent]] = {

    val details = validatedRowToEditDetails(hmdaRowValidatedError)

    val fDetails = details.map { detail =>
      val fDetailEvent: Future[EditDetailsPersistenceEvent] = editDetailPersistence ? (ref => PersistEditDetails(detail, Some(ref)))
      fDetailEvent
    }

    Future.sequence(fDetails)
  }

  private def publishSignEvent(
                                submissionId: SubmissionId,
                                email: String,
                                signedTimestamp: Long,
                                config: Config
                              )(implicit system: ActorSystem[_], materializer: Materializer, ec: ExecutionContext): Future[Done] =
    for {
      _ <- produceRecord(signTopic, submissionId.lei, submissionId.toString, stringKafkaProducer)
      _ <- produceRecord(emailTopic, s"${submissionId.toString}-${signedTimestamp}", email, stringKafkaProducer)
    } yield Done

  private def setHmdaFilerFlag(institutionID: String, period: Period, sharding: ClusterSharding)(
    implicit ec: ExecutionContext,
    t: Timeout
  ): Unit = {

    val year       = period.year.toString
    val periodType = period.toString

    val institutionPersistence =
      if (year == "2018") {
        sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$institutionID")
      } else {
        sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$institutionID-$year")
      }

    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (ref => GetInstitution(ref))

    fInstitution.foreach { maybeInst =>
      val institution = maybeInst.getOrElse(Institution.empty)

      val modifiedInstitution = periodType match {
        case quarterlyRegexQ1(_*) => institution.copy(quarterlyFilerHasFiledQ1 = true)
        case quarterlyRegexQ2(_*) => institution.copy(quarterlyFilerHasFiledQ2 = true)
        case quarterlyRegexQ3(_*) => institution.copy(quarterlyFilerHasFiledQ3 = true)
        case _                    => institution.copy(hmdaFiler = true)
      }
      if (institution.LEI.nonEmpty) {

        val modified: Future[InstitutionEvent] =
          institutionPersistence ? (ref => ModifyInstitution(modifiedInstitution, ref))
        modified
      } else ()
    }
  }

  def selectHmdaValidationError(sharding: ClusterSharding, submissionId: SubmissionId): EntityRef[SubmissionProcessingCommand] =
    sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-$submissionId")
}
// $COVERAGE-ON$