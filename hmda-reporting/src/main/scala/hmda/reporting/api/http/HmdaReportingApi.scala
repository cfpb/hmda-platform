package hmda.reporting.api.http

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi

import scala.concurrent.ExecutionContext

// $COVERAGE-OFF$
object HmdaReportingApi {
  val name: String = "hmda-reporting-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val classic: ActorSystem = ctx.system.toClassic
    implicit val ec: ExecutionContext = ctx.executionContext
    val shutdown                      = CoordinatedShutdown(classic)
    val config                        = ctx.system.settings.config
    val host: String                  = config.getString("hmda.reporting.http.host")
    val port: Int                     = config.getInt("hmda.reporting.http.port")
    val routes                        = BaseHttpApi.routes(name) ~ ReportingHttpApi.create(config)

    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.empty
  }
}
// $COVERAGE-ON$