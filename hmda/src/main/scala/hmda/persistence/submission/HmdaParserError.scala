package hmda.persistence.submission

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.messages.pubsub.KafkaTopics.uploadTopic
import hmda.messages.submission.SubmissionProcessingCommands.{HmdaParserStop, StartParsing, SubmissionProcessingCommand}
import hmda.messages.submission.SubmissionProcessingEvents.{HmdaRowParsed, SubmissionProcessingEvent}
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.ParserFlow.parseHmdaFile
import hmda.persistence.HmdaTypedPersistentActor
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer

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
                     HmdaParserErrorState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case StartParsing(submissionId) =>
        log.info(s"Start parsing for ${submissionId.toString}")
        uploadConsumer(ctx, submissionId)
          .map(_.value())
          .map(ByteString(_))
          .via(parseHmdaFile)
          .filter(x => x.isLeft)
          .collect {
            case Left(errors) => errors
          }

        Effect.none
      case _ =>
        Effect.none
    }
  }

  override def eventHandler: (
      HmdaParserErrorState,
      SubmissionProcessingEvent) => HmdaParserErrorState = {
    case (state, HmdaRowParsed()) => state
    case (state, _)               => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding, HmdaParserStop)
  }

  def uploadConsumer(ctx: ActorContext[_], submissionId: SubmissionId): Source[ConsumerRecord[String, String], Consumer.Control] = {
    val kafkaConfig = ctx.asScala.system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics(uploadTopic))
        .filter(_.key() == submissionId.toString)

  }

}
