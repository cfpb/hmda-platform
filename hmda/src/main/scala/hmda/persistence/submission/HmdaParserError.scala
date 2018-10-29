package hmda.persistence.submission

import akka.NotUsed
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
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import hmda.messages.pubsub.KafkaTopics._
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsed,
  HmdaRowParsedError,
  SubmissionProcessingEvent
}
import hmda.model.filing.submission.SubmissionId
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.ParserFlow._
import hmda.persistence.HmdaTypedPersistentActor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ConsumerMessage.CommittableMessage
import com.typesafe.config.ConfigFactory

object HmdaParserError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaParserErrorState] {

  override val name: String = "HmdaParserError"

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
                     HmdaParserErrorState] = { (_, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case StartParsing(submissionId) =>
        implicit val system: ActorSystem = ctx.asScala.system.toUntyped
        implicit val materializer: ActorMaterializer = ActorMaterializer()
        log.info(s"Start parsing for ${submissionId.toString}")
        uploadConsumer(ctx, submissionId)
          .map(_.record.value())
          .map(ByteString(_))
          .via(parseHmdaFile)
          .filter(x => x.isLeft)
          .zip(Source.fromIterator(() => Iterator.from(1)))
          .map {
            case (Left(errors), rowNumber) =>
              PersistHmdaRowParsedError(rowNumber, errors.map(_.errorMessage))
            case (Right(hmdaFileRow), _) => PersistHmdaRowParsed(hmdaFileRow)
          }
          .runWith(Sink.actorRef(ctx.asScala.self.toUntyped,
                                 CompleteParsing(submissionId)))
        Effect.none

      case PersistHmdaRowParsedError(rowNumber, errors) =>
        Effect.persist(HmdaRowParsedError(rowNumber, errors)).thenRun { _ =>
          log.info(s"Persisted error: $rowNumber, $errors")
        }

      case _ =>
        Effect.none
    }
  }

  override def eventHandler: (
      HmdaParserErrorState,
      SubmissionProcessingEvent) => HmdaParserErrorState = {
    case (state, HmdaRowParsed(_)) => state
    case (state, _)                => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding, HmdaParserStop)
  }

  private def uploadConsumer(ctx: ActorContext[_], submissionId: SubmissionId)
    : Source[CommittableMessage[String, String], Consumer.Control] = {
    val config = ConfigFactory.load()
    val kafkaHosts = config.getString("kafka.hosts")
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

  private def parserProducer(topic: String, submissionId: SubmissionId)
    : Flow[List[ParserValidationError], String, NotUsed] =
    Flow[List[ParserValidationError]]
      .map(xs => xs.map(_.errorMessage).mkString("\n"))

}
