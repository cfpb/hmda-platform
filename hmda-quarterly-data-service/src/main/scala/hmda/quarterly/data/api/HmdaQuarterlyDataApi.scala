package hmda.quarterly.data.api

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import hmda.api.http.directives.HmdaTimeDirectives.timed
import hmda.api.http.routes.BaseHttpApi
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

object HmdaQuarterlyDataApi {

  val name: String = "hmda-quarterly-data"

  val main: Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val classic: actor.ActorSystem = system.toClassic
    implicit val mat: Materializer = Materializer(ctx)
    implicit val ec: ExecutionContext = ctx.executionContext
    implicit val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("db")
    val log = ctx.log
    val config = system.settings.config
    val host: String = config.getString("server.bindings.address")
    val port: Int = config.getInt("server.bindings.port")
    val shutdown = CoordinatedShutdown(system)

//    val oAuth2Authorization = OAuth2Authorization(log, config)

    val routes = cors() {
      BaseHttpApi.routes(name) ~ HmdaQuarterlyGraphRequestHandler.routes
    }
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.ignore
  }
}
