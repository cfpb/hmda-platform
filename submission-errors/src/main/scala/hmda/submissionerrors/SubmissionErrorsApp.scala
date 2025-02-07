package hmda.submissionerrors

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ CoordinatedShutdown, ActorSystem => ClassicActorSystem }
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions }
import akka.stream.scaladsl.{ RunnableGraph, Source }
import akka.stream.{ ActorAttributes, Supervision }
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.HmdaGroups
import hmda.submissionerrors.repositories.PostgresSubmissionErrorRepository
import hmda.submissionerrors.streams.SubmissionProcessor.{ handleMessages, processRawKafkaSubmission }
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import hmda.publication.KafkaUtils._

// $COVERAGE-OFF$
object SubmissionErrorsApp extends App {
  val config         = ConfigFactory.load()
  val kafkaHosts     = config.getString("kafka.hosts")
  val kafkaTopic     = config.getString("kafka.topic")
  val databaseTable  = config.getString("dbconfig.table")
  val databaseConfig = PostgresSubmissionErrorRepository.config("submission-errors-db")

  val classicSystem: ClassicActorSystem    = ClassicActorSystem("submission-errors-app", config)
  implicit val typedSystem: ActorSystem[_] = classicSystem.toTyped
  implicit val monixScheduler: Scheduler   = Scheduler(classicSystem.toTyped.executionContext)

  val repo = PostgresSubmissionErrorRepository.make(databaseConfig, databaseTable)

  val kafkaConsumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(
      system = classicSystem,
      keyDeserializer = new StringDeserializer,
      valueDeserializer = new StringDeserializer
    ).withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.submissionErrorsGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperties(getKafkaConfig)      

  val kafkaCommitterSettings: CommitterSettings =
    CommitterSettings(classicSystem)

  val kafkaConsumerSource: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
    Consumer.committableSource(kafkaConsumerSettings, Subscriptions.topics(kafkaTopic))

  val decider: Supervision.Decider = { _: Throwable => Supervision.Restart }

  val graph: RunnableGraph[Consumer.Control] =
    kafkaConsumerSource
      .via(processRawKafkaSubmission)
      .to(handleMessages(repo, kafkaCommitterSettings))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))

  val killSwitch = graph.run()

  CoordinatedShutdown(classicSystem)
    .addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "stop-kafka-consumer")(() => killSwitch.shutdown())
}
// $COVERAGE-ON$
