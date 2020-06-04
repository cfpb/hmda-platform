package hmda.calculator.api.http

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi

import scala.concurrent.ExecutionContext
// $COVERAGE-OFF$
object RateSpreadAPI {
  val name: String = "hmda-ratespread-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem  = ctx.system.toClassic
    implicit val ec: ExecutionContext = ctx.executionContext
    val log                           = ctx.log
    val shutdown                      = CoordinatedShutdown(system)
    val config                        = system.settings.config
    val host: String                  = config.getString("hmda.ratespread.http.host")
    val port: Int                     = config.getInt("hmda.ratespread.http.port")

    val routes = BaseHttpApi.routes(name) ~ RateSpreadAPIRoutes.create(log)
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)

    Behaviors.empty
  }
}
// $COVERAGE-ON$