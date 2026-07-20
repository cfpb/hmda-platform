package hmda.persistence.migrator

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.{ as, complete, cors, entity, path }
import akka.http.scaladsl.server.Route
import akka.persistence.r2dbc.migration.MigrationTool
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives.timed
import hmda.api.http.routes.BaseHttpApi
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

object HmdaPersistenceAdhocMigrationServer {
  val name = "HmdaPersistenceAdhocMigrationServer"

  private val log = LoggerFactory.getLogger(getClass)

  val main: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val classic: actor.ActorSystem = system.toClassic
    implicit val ec: ExecutionContextExecutor = context.executionContext
    implicit val timeout: Timeout = 5.minutes
    val migration = new MigrationTool(system)
    val shutdown = CoordinatedShutdown(system)
    val host: String = system.settings.config.getString("server.bindings.address")
    val port: Int = system.settings.config.getInt("server.bindings.port")

    val persistenceIdPrefixes = List("Filing", "Submission", "HmdaRawData", "HmdaParserError", "HmdaValidationError", "EditDetail")

    val appRoute: Route = {
      path("migrate") {
        post {
          entity(as[Map[String, String]]) { payload =>
            val leiParam = payload.get("lei")
            val yearParam = payload.get("year")
            val endSeq = payload.getOrElse("end", "0").toInt

            if (leiParam.isEmpty || yearParam.isEmpty) {
              complete("No lei or Year Provided")
            } else {
              val lei = leiParam.get
              val year = yearParam.get

              val futures = ListBuffer[Future[Map[String, Map[String, Long]]]]()
              for (i <- 1 to endSeq) {
                persistenceIdPrefixes.foreach { prefix =>
                  val pid = f"$prefix-$lei-$year-$i"
                  val pidMigrations = for {
                    eventsMigrated <- migration.migrateEvents(pid)
                    snapshotMigrated <- migration.migrateSnapshot(pid).map(_.toLong)
                  } yield Map(pid -> Map("event" -> eventsMigrated, "snapshot" -> snapshotMigrated))

                  futures += pidMigrations
                }
              }
              val allFutures = Future.sequence(futures.toList)

              onComplete(allFutures) {
                case Success(res) =>
                  log.info("completed migration\n{}", res)
                  complete(res)
                case Failure(e) =>
                  log.error("failed migration", e)
                  complete("failed")
              }
            }
          }
        }
      }
    }

    val routes = cors() {
      BaseHttpApi.routes(name) ~ appRoute
    }
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.ignore
  }
}
