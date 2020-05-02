package hmda.dashboard.api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.dashboard.Settings

import scala.concurrent.ExecutionContextExecutor

object HmdaDashboardApi extends Settings {
  val name: String = "hmda-dashboard"
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem          = ctx.system.toClassic
    implicit val ec: ExecutionContextExecutor = ctx.executionContext
    val log                                   = ctx.log
    val config                                = system.settings.config
    val shutdown                              = CoordinatedShutdown(system)

    val routes = BaseHttpApi.routes(name) ~ HmdaDashboardHttpApi.create(log, config)
    val host   = server.host
    val port   = server.port
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)

    Behaviors.ignore
  }
}