package hmda.quarterly.data.api

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import hmda.api.http.directives.HmdaTimeDirectives.timed
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.OAuth2Authorization
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext

object HmdaQuarterlyDataApi {

  val name: String = "hmda-quarterly-data"

  val main: Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val classic: actor.ActorSystem = system.toClassic
    implicit val mat: Materializer = Materializer(ctx)
    implicit val ec: ExecutionContext = ctx.executionContext
    val log = ctx.log
    val config = system.settings.config
    val host: String = config.getString("server.bindings.address")
    val port: Int = config.getInt("server.bindings.port")
    val shutdown = CoordinatedShutdown(system)

//    val oAuth2Authorization = OAuth2Authorization(log, config)

    val routes = cors() {
      BaseHttpApi.routes(name) ~ HmdaQuarterlyDataRequestHandler.routes
    }
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.ignore
  }
}
