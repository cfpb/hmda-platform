package hmda.dashboard.api


import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.{CoordinatedShutdown, ActorSystem => ClassicActorSystem}
import org.apache.pekko.stream.Materializer
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.OAuth2Authorization

import scala.concurrent.ExecutionContext

object HmdaDashboardApi {
  val name: String = "hmda-dashboard"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[_] = ctx.system
    implicit val classic: ClassicActorSystem = system.toClassic
    implicit val mat: Materializer           = Materializer(ctx)
    implicit val ec: ExecutionContext        = ctx.executionContext
    val log                                   = ctx.log
    val config                                = system.settings.config
    val host: String                         = config.getString("server.bindings.address")
    val port: Int                            = config.getInt("server.bindings.port")
    val shutdown                              = CoordinatedShutdown(system)

    val oAuth2Authorization = OAuth2Authorization(log, config)
    val dashboardRoutes = HmdaDashboardHttpApi.create(log, config)
    val routes = dashboardRoutes(oAuth2Authorization)

    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.empty
  }
}