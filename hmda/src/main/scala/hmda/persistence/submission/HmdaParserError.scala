package hmda.persistence.submission

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import hmda.messages.pubsub.KafkaTopics._
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError,
  SubmissionProcessingEvent
}
import hmda.model.filing.submission.{
  Parsed,
  ParsedWithErrors,
  Submission,
  SubmissionId
}
import hmda.parser.filing.ParserFlow._
import hmda.persistence.HmdaTypedPersistentActor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ConsumerMessage.CommittableMessage
import com.typesafe.config.ConfigFactory
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object HmdaParserError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaParserErrorState] {

  override val name: String = "HmdaParserError"

  val config = ConfigFactory.load()
  val kafkaHosts = config.getString("kafka.hosts")
  val kafkaIdleTimeout = config.getInt("kafka.idle-timeout")
  val futureTimeout = config.getInt("hmda.actor.timeout")

  implicit val timeout: Timeout = Timeout(futureTimeout.seconds)

  override def behavior(
      entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[SubmissionProcessingCommand,
                 SubmissionProcessingEvent,
                 HmdaParserErrorState](
          persistenceId = s"$entityId",
          emptyState = HmdaParserErrorState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
    }

  override def commandHandler(ctx: ActorContext[SubmissionProcessingCommand])
    : CommandHandler[SubmissionProcessingCommand,
                     SubmissionProcessingEvent,
                     HmdaParserErrorState] = { (state, cmd) =>
    val log = ctx.asScala.log
    implicit val system: ActorSystem = ctx.asScala.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    cmd match {
      case StartParsing(submissionId) =>
        log.info(s"Start parsing for ${submissionId.toString}")

        uploadConsumer(ctx, submissionId)
          .map(_.record.value())
          .map(ByteString(_))
          .via(parseHmdaFile)
          .zip(Source.fromIterator(() => Iterator.from(1)))
          .map {
            case (Left(errors), rowNumber) =>
              PersistHmdaRowParsedError(rowNumber, errors.map(_.errorMessage))
            case (Right(hmdaFileRow), _) => HmdaRowParsed(hmdaFileRow)
          }
          .idleTimeout(kafkaIdleTimeout.seconds)
          .runForeach(msg => ctx.asScala.self ! msg)
          .onComplete {
            case Success(_) =>
              log.debug(s"stream completed for ${submissionId.toString}")
            case Failure(_) =>
              ctx.asScala.self ! CompleteParsing(submissionId)
          }
        Effect.none

      case PersistHmdaRowParsedError(rowNumber, errors) =>
        Effect.persist(HmdaRowParsedError(rowNumber, errors)).thenRun { _ =>
          log.info(s"Persisted error: $rowNumber, $errors")
        }

      case HmdaRowParsed(hmdaFileRow) =>
        log.debug(s"${hmdaFileRow.toString}")
        Effect.none

      case GetParsedWithErrorCount(replyTo) =>
        replyTo ! HmdaRowParsedCount(state.linesWithErrorCount)
        Effect.none

      case CompleteParsing(submissionId) =>
        log.info(
          s"Completed Parsing for ${submissionId.toString}, total lines with errors: ${state.linesWithErrorCount}")
        updateSubmissionStatus(ctx, state, submissionId)
        Effect.none

      case HmdaParserStop =>
        Effect.stop

      case _ =>
        Effect.none
    }
  }

  override def eventHandler: (
      HmdaParserErrorState,
      SubmissionProcessingEvent) => HmdaParserErrorState = {
    case (state, HmdaRowParsedError(_, _)) =>
      state.incrementErrorCount
    case (state, _) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding, HmdaParserStop)
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

  private def updateSubmissionStatus(
      ctx: ActorContext[_],
      state: HmdaParserErrorState,
      submissionId: SubmissionId)(implicit ec: ExecutionContext) = {
    val sharding = ClusterSharding(ctx.asScala.system)

    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey,
                            s"${SubmissionPersistence.name}-$submissionId")

    val submissionManager =
      sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (
        ref => GetSubmission(ref))

    fSubmission.map {
      case Some(submission) =>
        val modified = if (state.linesWithErrorCount == 0) {
          submission.copy(status = Parsed)
        } else {
          submission.copy(status = ParsedWithErrors)
        }
        submissionManager ! UpdateSubmissionStatus(modified)
      case None =>
        ctx.asScala.log
          .error(s"Submission ${submissionId.toString} could not be retrieved")
    }

  }

}
