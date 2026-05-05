package hmda.submissionerrors

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions }
import akka.stream.scaladsl.{ RunnableGraph, Source }
import akka.stream.{ ActorAttributes, Materializer, Supervision }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import hmda.api.http.directives.HmdaTimeDirectives.timed
import hmda.api.http.routes.BaseHttpApi
import hmda.messages.pubsub.HmdaGroups
import hmda.publication.KafkaUtils
import hmda.publication.KafkaUtils._
import hmda.submissionerrors.repositories.PostgresSubmissionErrorRepository
import hmda.submissionerrors.streams.SubmissionProcessor.{ handleMessages, processRawKafkaSubmission }
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.util.{ Failure, Success }

// $COVERAGE-OFF$
object SubmissionErrorsApp extends App {

  val name = "submission-errors-app"

  val main: Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val classicSystem: actor.ActorSystem = system.toClassic
    implicit val mat: Materializer = Materializer(ctx)
    val log = ctx.log
    val config = system.settings.config
    val host: String = config.getString("server.bindings.address")
    val port: Int = config.getInt("server.bindings.port")
    val shutdown = CoordinatedShutdown(system)
    val kafkaHosts     = config.getString("kafka.hosts")
    val kafkaTopic     = config.getString("kafka.topic")
    val databaseTable  = config.getString("dbconfig.table")
    val databaseConfig = PostgresSubmissionErrorRepository.config("submission-errors-db")

    implicit val monixScheduler: Scheduler   = Scheduler(system.executionContext)

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

    val stringKafkaProducer = KafkaUtils.getStringKafkaProducer(system)

    val decider: Supervision.Decider = { _: Throwable => Supervision.Restart }

    val graph: RunnableGraph[Consumer.Control] =
      kafkaConsumerSource
        .via(processRawKafkaSubmission)
        .to(handleMessages(repo, kafkaCommitterSettings))
        .withAttributes(ActorAttributes.supervisionStrategy(decider))

    val killSwitch = graph.run()

    val appRoutes: Route = {
      pathPrefix("key") {
        path( Segment ) { key => {
          onComplete(KafkaUtils.produceRecord(kafkaTopic, "", key, stringKafkaProducer)) {
            case Success(_) => complete(f"sent $key")
            case Failure(exception) =>
              log.error("failed to send {}", key, exception)
              complete("failed")
          }
        }}
      }
    }

    val routes = cors() {
      BaseHttpApi.routes(name) ~ appRoutes
    }

    CoordinatedShutdown(classicSystem)
      .addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "stop-kafka-consumer")(() => killSwitch.shutdown())

    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.ignore
  }

  ActorSystem[Nothing](main, name)
}
// $COVERAGE-ON$
