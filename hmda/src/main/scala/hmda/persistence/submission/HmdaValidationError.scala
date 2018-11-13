package hmda.persistence.submission

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior, Logger}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.KafkaTopics.uploadTopic
import hmda.messages.submission.SubmissionProcessingCommands.{
  GetHmdaValidationErrorState,
  PersistHmdaRowValidatedError,
  StartSyntacticalValidity,
  SubmissionProcessingCommand
}
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
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.model.validation.{TsValidationError, ValidationError}
import hmda.persistence.institution.InstitutionPersistence
import hmda.validation.context.ValidationContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import hmda.parser.filing.ParserFlow._
import hmda.validation.HmdaValidated
import hmda.validation.engine.TsEngine
import hmda.validation.filing.ValidationFlow._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Success

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
        log.info(s"Start Syntactical / Validity validation for $submissionId")

        val fValidationContext =
          validationContext(processingYear, sharding, ctx, submissionId)

        for {
          validationContext <- fValidationContext
          tsErrors = validateTs("all",
                                ctx,
                                sharding,
                                submissionId,
                                validationContext)
          errors = syntacticalValidityErrors(ctx,
                                             sharding,
                                             submissionId,
                                             validationContext.ts)
        } yield {
          tsErrors.foreach { error =>
            ctx.asScala.self ! PersistHmdaRowValidatedError(1, error, None)
          }
          errors.map(e => e.v)

        }

        //validateLar(ctx, sharding, submissionId, fTs)

        Effect.none

      case PersistHmdaRowValidatedError(rowNumber,
                                        validationError,
                                        maybeReplyTo) =>
        Effect
          .persist(HmdaRowValidatedError(rowNumber, validationError))
          .thenRun { _ =>
            log.debug(s"Persisted: ${validationError.toCsv}")
            println(s"Persisted: ${validationError.toCsv}")
            maybeReplyTo match {
              case Some(replyTo) =>
                replyTo ! HmdaRowValidatedError(rowNumber, validationError)
              case None => //do nothing
            }
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

  //  private def larFlow(ctx: ActorContext[SubmissionProcessingCommand], submissionId: SubmissionId) =
  //    uploadConsumerRawStr(ctx, submissionId)
  //    .drop(1)
  //    .via(parseLarFlow)
  //    .map(_.getOrElse(LoanApplicationRegister()))

  private def validateTs(
      checkType: String,
      ctx: ActorContext[SubmissionProcessingCommand],
      sharding: ClusterSharding,
      submissionId: SubmissionId,
      validationContext: ValidationContext): List[ValidationError] = {

    val ts = validationContext.ts.getOrElse(TransmittalSheet())
    val tsSyntacticalErrorList: List[ValidationError] = TsEngine
      .checkSyntactical(ts, ts.LEI, validationContext, TsValidationError)
      .leftMap(errors => errors.toList)
      .swap
      .toList
      .flatten
    val tsValidityErrorList: List[ValidationError] = TsEngine
      .checkValidity(ts, ts.LEI, TsValidationError)
      .leftMap(errors => errors.toList)
      .swap
      .toList
      .flatten
    tsSyntacticalErrorList ++ tsValidityErrorList

  }

  private def syntacticalValidityErrors(
      ctx: ActorContext[SubmissionProcessingCommand],
      sharding: ClusterSharding,
      submissionId: SubmissionId,
      ts: Option[TransmittalSheet])(implicit materializer: ActorMaterializer,
                                    ec: ExecutionContext) = {
    val institutionPersistence =
      sharding.entityRefFor(
        InstitutionPersistence.typeKey,
        s"${InstitutionPersistence.name}-${submissionId.lei}")

    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
        ref => GetInstitution(ref))

    for {
      institution <- fInstitution
      syntacticalErrors = larErrors("syntactical",
                                    ctx,
                                    submissionId,
                                    ts,
                                    institution)
      validityErrors = larErrors("validity", ctx, submissionId, ts, institution)

    } yield {
      syntacticalErrors.concat(validityErrors)
    }
  }

  private def larErrors(checkType: String,
                        ctx: ActorContext[SubmissionProcessingCommand],
                        submissionId: SubmissionId,
                        ts: Option[TransmittalSheet],
                        institution: Option[Institution])(
      implicit materializer: ActorMaterializer) = {
    uploadConsumerRawStr(ctx, submissionId)
      .drop(1)
      .via(
        validateLarFlow(
          checkType,
          ValidationContext(institution, Some(processingYear), ts)))
      .mapConcat(_.swap.getOrElse(Nil))
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .map(errorWithIndex =>
        PersistHmdaRowValidatedError(errorWithIndex._2,
                                     errorWithIndex._1,
                                     None))
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
