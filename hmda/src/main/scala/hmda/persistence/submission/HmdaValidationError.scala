package hmda.persistence.submission

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior, Logger}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.KafkaTopics.uploadTopic
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowValidatedError,
  SubmissionProcessingEvent
}
import hmda.model.filing.submission.{
  Submission,
  SubmissionId,
  SubmissionStatus,
  Validating
}
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.persistence.HmdaTypedPersistentActor
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.ActorMaterializer
import akka.stream.typed.scaladsl.ActorFlow
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import hmda.validation.context.ValidationContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import hmda.parser.filing.ParserFlow._
import hmda.validation.filing.ValidationFlow._

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
          errors <- validateTs(ctx, submissionId, validationContext)
            .concat(
              validateLar("syntactical", ctx, submissionId, validationContext))
            .concat(
              validateLar("validity", ctx, submissionId, validationContext))
            .idleTimeout(kafkaIdleTimeout.seconds)
            .runWith(Sink.ignore)
        } yield errors

        fSyntacticalValidity.onComplete {
          case Success(_) =>
            log.debug(s"stream completed for ${submissionId.toString}")
          case Failure(_) =>
            ctx.asScala.self ! CompleteSyntacticalValidity(submissionId)
        }

        Effect.none

      case PersistHmdaRowValidatedError(rowNumber,
                                        validationErrors,
                                        maybeReplyTo) =>
        Effect
          .persist(HmdaRowValidatedError(rowNumber, validationErrors))
          .thenRun { _ =>
            log.info(
              s"Persisted: ${HmdaRowValidatedError(rowNumber, validationErrors)}")
            maybeReplyTo match {
              case Some(replyTo) =>
                replyTo ! HmdaRowValidatedError(rowNumber, validationErrors)
              case None => //do nothing
            }
          }

      //TODO: serialization for this message
      case CompleteSyntacticalValidity(submissionId) =>
        log.info(
          s"Syntactical / Validity validation finished for $submissionId")
        //TODO: update submission manager
        Effect.none

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
      state.update(error)
    case (state, _) => state
  }

  private def uploadConsumer(ctx: ActorContext[_], submissionId: SubmissionId)
    : Source[CommittableMessage[String, String], Consumer.Control] = {
    val kafkaConfig =
      ctx.asScala.system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(kafkaConfig,
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(kafkaHosts)
        .withGroupId(submissionId.toString)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(uploadTopic))
      .filter(_.record.key() == submissionId.toString)

  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding)
  }

  private def updateSubmissionStatus(
      sharding: ClusterSharding,
      submissionId: SubmissionId,
      modified: SubmissionStatus,
      log: Logger)(implicit ec: ExecutionContext): Unit = {
    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey,
                            s"${SubmissionPersistence.name}-$submissionId")

    val submissionManager =
      sharding.entityRefFor(SubmissionManager.typeKey,
                            s"${SubmissionManager.name}-$submissionId")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (
        ref => GetSubmission(ref))

    for {
      m <- fSubmission
      s = m.getOrElse(Submission())
    } yield {
      if (s.isEmpty) {
        log
          .error(s"Submission $submissionId could not be retrieved")
      } else {
        val modifiedSubmission = s.copy(status = modified)
        submissionManager ! UpdateSubmissionStatus(modifiedSubmission)
      }
    }
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
          PersistHmdaRowValidatedError(rowNumber, errors, None)
      }
      .via(
        ActorFlow.ask(ctx.asScala.self)(
          (el, replyTo: ActorRef[HmdaRowValidatedError]) =>
            PersistHmdaRowValidatedError(el.rowNumber,
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
          PersistHmdaRowValidatedError(rowNumber, errors, None)
      }
      .via(
        ActorFlow.ask(ctx.asScala.self)(
          (el, replyTo: ActorRef[HmdaRowValidatedError]) =>
            PersistHmdaRowValidatedError(el.rowNumber,
                                         el.validationErrors,
                                         Some(replyTo))
        ))
  }

//  private def validateTs(
//      checkType: String,
//      ctx: ActorContext[SubmissionProcessingCommand],
//      sharding: ClusterSharding,
//      submissionId: SubmissionId,
//      validationContext: ValidationContext): List[ValidationError] = {
//
//    val ts = validationContext.ts.getOrElse(TransmittalSheet())
//    val tsSyntacticalErrorList: List[ValidationError] = TsEngine
//      .checkSyntactical(ts, ts.LEI, validationContext, TsValidationError)
//      .leftMap(errors => errors.toList)
//      .swap
//      .toList
//      .flatten
//    val tsValidityErrorList: List[ValidationError] = TsEngine
//      .checkValidity(ts, ts.LEI, TsValidationError)
//      .leftMap(errors => errors.toList)
//      .swap
//      .toList
//      .flatten
//    tsSyntacticalErrorList ++ tsValidityErrorList
//
//  }

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
    uploadConsumer(ctx, submissionId)
      .map(_.record.value())
      .map(ByteString(_))
  }

}
