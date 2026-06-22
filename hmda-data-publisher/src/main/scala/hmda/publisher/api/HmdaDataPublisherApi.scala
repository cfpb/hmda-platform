package hmda.publisher.api

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.actor.{ActorSystem, CoordinatedShutdown}
import pekko.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.publisher.scheduler.AllSchedulers

import scala.concurrent.ExecutionContext

// $COVERAGE-OFF$
object HmdaDataPublisherApi {
  val name = "hmda-data-publisher-api"

  def apply(allSchedulers: AllSchedulers): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val ec: ExecutionContext = ctx.executionContext
    implicit val classic: ActorSystem = ctx.system.toClassic
    val shutdown: CoordinatedShutdown = CoordinatedShutdown(ctx.system)
    val config                        = classic.settings.config
    val host: String                  = config.getString("hmda.publisher.http.host")
    val port: Int                     = config.getInt("hmda.publisher.http.port")

    val routes = BaseHttpApi.routes(name) ~ new DataPublisherHttpApi(allSchedulers).routes
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)

    Behaviors.ignore
  }
}
// $COVERAGE-ON$