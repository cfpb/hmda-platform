package hmda.authService.api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.routes.BaseHttpApi
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.auth.OAuth2Authorization
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
object HmdaAuthApi {
  val name: String = "hmda-auth-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem  = ctx.system.toClassic
    implicit val ec: ExecutionContext = ctx.executionContext
    val shutdown                      = CoordinatedShutdown(system)
    val config                        = ctx.system.settings.config
    implicit val timeout: Timeout     = Timeout(config.getInt("hmda.auth.http.timeout").seconds)
    val log                           = ctx.log
    val oAuth2Authorization           = OAuth2Authorization(log, config)
    val authRoute                     = AuthHttpApi.create(log)
    val routes                        = BaseHttpApi.routes(name) ~ authRoute(oAuth2Authorization)
    val host: String                  = config.getString("hmda.auth.http.host")
    val port: Int                     = config.getInt("hmda.auth.http.port")

    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.empty
  }
}
// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
