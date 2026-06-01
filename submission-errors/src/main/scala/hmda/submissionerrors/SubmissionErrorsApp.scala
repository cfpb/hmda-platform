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
import hmda.query.DbConfiguration.dbConfig
import hmda.submissionerrors.repositories.{ PostgresSubmissionErrorRepository, PostgresSubmissionSummaryRepository }
import hmda.submissionerrors.streams.SubmissionProcessor.{ handleMessages, processRawKafkaSubmission }
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
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
    val summaryTable = config.getString("dbconfig.summaryTable")
    val databaseConfig = PostgresSubmissionErrorRepository.config("submission-errors-db")
    val tsTablePrefix = config.getString("dbconfig.tsTablePrefix")

    implicit val monixScheduler: Scheduler   = Scheduler(system.executionContext)

    val repo = PostgresSubmissionErrorRepository.make(databaseConfig, databaseTable)
    val summaryRepo = PostgresSubmissionSummaryRepository.make(databaseConfig, summaryTable)

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
        .to(handleMessages(repo, summaryRepo, kafkaCommitterSettings))
        .withAttributes(ActorAttributes.supervisionStrategy(decider))

    val killSwitch = graph.run()

    val appRoutes: Route = {
      path("run") {
        post {
          entity(as[Map[String, Int]]) { payload =>
            val yearParam = payload.get("year")

            yearParam.map(year => {
              import dbConfig._
              import dbConfig.profile.api._

              val tsName = f"$tsTablePrefix$year"

              onComplete(db.run(sql"SELECT DISTINCT lei FROM #$tsName".as[String])) {
                case Success(value) =>
                  onComplete(Future.sequence(value.map(lei => KafkaUtils.produceRecord(kafkaTopic, "", f"$lei:$year", stringKafkaProducer)))) {
                    case Success(_) =>
                      complete(f"All leis for year $year sent")
                    case Failure(e) =>
                      log.error("Failed!", e)
                      complete("Failed to send to kafka, check server logs")
                  }
                case Failure(exception) =>
                  log.error("failed", exception)
                  complete(f"Failed to get LEIs from $year, check server logs")
              }
            }).getOrElse(
              complete("Need to provide \"year\" parameter")
            )
          }
        }
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
