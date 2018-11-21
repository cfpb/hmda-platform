package hmda.persistence.submission

import java.time.Instant

import akka.actor.ActorSystem
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
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import hmda.validation.context.ValidationContext
import hmda.parser.filing.ParserFlow._
import hmda.validation.filing.ValidationFlow._
import HmdaProcessingUtils._
import hmda.messages.pubsub.KafkaTopics.uploadTopic
import EditDetailsConverter._
import akka.cluster.sharding.typed.scaladsl.EntityRef
import hmda.messages.submission.EditDetailsCommands.{
  EditDetailsPersistenceCommand,
  PersistEditDetails
}
import hmda.messages.submission.EditDetailsEvents.EditDetailsPersistenceEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object HmdaValidationError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaValidationErrorState] {

  override val name: String = "HmdaValidationError"

  val config = ConfigFactory.load()
  val kafkaHosts = config.getString("kafka.hosts")
  val kafkaIdleTimeout = config.getInt("kafka.idle-timeout")
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
      ).withTagger(_ => Set("validate"))
        .snapshotEvery(1000)
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
            .idleTimeout(kafkaIdleTimeout.seconds)
            .runWith(Sink.ignore)
          larSyntacticalValidityErrors <- validateLar("syntactical-validity",
                                                      ctx,
                                                      submissionId,
                                                      validationContext)
            .idleTimeout(kafkaIdleTimeout.seconds)
            .runWith(Sink.ignore)
        } yield (tsErrors, larSyntacticalValidityErrors)

        fSyntacticalValidity.onComplete {
          case Success(_) =>
            log.warning(
              s"Syntactical / Validity stream completed for ${submissionId.toString}")
          case Failure(_) =>
            ctx.asScala.self ! CompleteSyntacticalValidity(submissionId)
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
            .idleTimeout(kafkaIdleTimeout.seconds)
            .runWith(Sink.ignore)
        } yield larErrors

        fQuality.onComplete {
          case Success(_) =>
            log.warning(
              s"Quality stream completed for ${submissionId.toString}")
          case Failure(_) =>
            ctx.asScala.self ! CompleteQuality(submissionId)
        }
        Effect.none

      case CompleteQuality(submissionId) =>
        log.info(s"Quality validation finished for $submissionId")
        val updatedStatus =
          if (state.quality.nonEmpty) {
            QualityErrors
          } else if (state.macroVerified) {
            Verified
          } else {
            Quality
          }
        Effect
          .persist(QualityCompleted(submissionId, updatedStatus.code))
          .thenRun { _ =>
            updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
          }

      case PersistHmdaRowValidatedError(submissionId,
                                        rowNumber,
                                        validationErrors,
                                        maybeReplyTo) =>
        val editDetailPersistence = sharding
          .entityRefFor(EditDetailsPersistence.typeKey,
                        s"${EditDetailsPersistence.name}-$submissionId")

        Effect
          .persist(HmdaRowValidatedError(rowNumber, validationErrors))
          .thenRun { _ =>
            log.info(
              s"Persisted: ${HmdaRowValidatedError(rowNumber, validationErrors)}")
            maybeReplyTo match {
              case Some(replyTo) =>
                val hmdaRowValidatedError =
                  HmdaRowValidatedError(rowNumber, validationErrors)

                for {
                  _ <- persistEditDetails(editDetailPersistence,
                                          hmdaRowValidatedError)
                } yield {
                  replyTo ! hmdaRowValidatedError
                }

              case None => //do nothing
            }
          }

      case VerifyQuality(submissionId, verified, replyTo) =>
        if (List(Quality.code, QualityErrors.code, Macro.code, MacroErrors.code)
              .contains(state.statusCode)) {
          Effect
            .persist(
              QualityVerified(submissionId,
                              verified,
                              SubmissionStatus.valueOf(state.statusCode)))
            .thenRun { _ =>
              if (state.macroVerified) {
                val updatedStatus = Verified
                updateSubmissionStatus(sharding,
                                       submissionId,
                                       updatedStatus,
                                       log)
                replyTo ! QualityVerified(submissionId, verified, updatedStatus)
              }
            }
        } else {
          replyTo ! NotReadyToBeVerified(submissionId)
          Effect.none
        }

      case SignSubmission(submissionId, replyTo) =>
        if (state.statusCode == Verified.code) {
          val timestamp = Instant.now().toEpochMilli
          val signed = SubmissionSigned(submissionId, timestamp, Signed)
          if (state.qualityVerified && state.macroVerified) {
            Effect.persist(signed).thenRun { _ =>
              log.info(
                s"Submission $submissionId sined at ${Instant.ofEpochMilli(timestamp)}")
              updateSubmissionStatus(sharding, submissionId, Signed, log)
              updateSubmissionReceipt(
                sharding,
                submissionId,
                signed.timestamp,
                s"${signed.submissionId}-${signed.timestamp}",
                log)
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
    case (state, SyntacticalValidityCompleted(_, statusCode)) =>
      state.updateStatusCode(statusCode)
    case (state, QualityCompleted(_, statusCode)) =>
      state.updateStatusCode(statusCode)
    case (state, evt: QualityVerified) =>
      state.verifyQuality(evt)
    case (state, SubmissionSigned(_, _, _)) =>
      state.updateStatusCode(Signed.code)
    case (state, _) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding)
  }

  private def validateTs(ctx: ActorContext[SubmissionProcessingCommand],
                         submissionId: SubmissionId,
                         validationContext: ValidationContext) = {
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

  private def validateLar(editCheck: String,
                          ctx: ActorContext[SubmissionProcessingCommand],
                          submissionId: SubmissionId,
                          validationContext: ValidationContext) = {
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

  private def maybeTs(ctx: ActorContext[SubmissionProcessingCommand],
                      submissionId: SubmissionId)(
      implicit materializer: ActorMaterializer,
      ec: ExecutionContext): Future[Option[TransmittalSheet]] = {
    uploadConsumerRawStr(ctx, submissionId)
      .take(1)
      .via(parseTsFlow)
      .map(_.getOrElse(TransmittalSheet()))
      .runWith(Sink.seq)
      .map(xs => xs.headOption)
  }

  private def validationContext(year: Int,
                                sharding: ClusterSharding,
                                ctx: ActorContext[SubmissionProcessingCommand],
                                submissionId: SubmissionId)(
      implicit materializer: ActorMaterializer,
      ec: ExecutionContext): Future[ValidationContext] = {
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

  private def uploadConsumerRawStr(
      ctx: ActorContext[SubmissionProcessingCommand],
      submissionId: SubmissionId) = {
    uploadConsumer(ctx.asScala.system, submissionId, uploadTopic)
      .map(_.record.value())
      .map(ByteString(_))
  }

  private def persistEditDetails(
      editDetailPersistence: EntityRef[EditDetailsPersistenceCommand],
      hmdaRowValidatedError: HmdaRowValidatedError)(
      implicit ec: ExecutionContext)
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

}
