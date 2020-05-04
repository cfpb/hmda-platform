package hmda.dataBrowser.api

import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ Behavior, SupervisorStrategy }
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.dataBrowser.Settings

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object DataBrowserApi extends Settings {
  val name: String = "hmda-data-browser"

  def apply(): Behavior[Nothing] =
    Behaviors
      .supervise[Nothing] {
        Behaviors.setup[Nothing] { ctx =>
          implicit val system               = ctx.system
          implicit val classic              = system.toClassic
          implicit val mat                  = Materializer(ctx)
          implicit val ec: ExecutionContext = ctx.executionContext
          val shutdown                      = CoordinatedShutdown(system)
          val log                           = ctx.log
          val host: String                  = server.host
          val port: Int                     = server.port
          val routes                        = BaseHttpApi.routes(name) ~ DataBrowserHttpApi.create(log)
          BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
          Behaviors.ignore
        }
      }
      .onFailure(SupervisorStrategy.restartWithBackoff(1.second, 30.seconds, 0.01))
}