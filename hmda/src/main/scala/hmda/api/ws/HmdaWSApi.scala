package hmda.api.ws

import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.{ actor, Done }
import hmda.api.ws.filing.submissions.SubmissionWsApi
import hmda.api.ws.routes.BaseWsApi

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

// This is just a guardian
// $COVERAGE-OFF$
object HmdaWSApi {
  val name = "hmda-ws-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[_]           = ctx.system
    implicit val mat: Materializer                = Materializer(ctx)
    implicit val classicSystem: actor.ActorSystem = system.toClassic
    implicit val ec: ExecutionContext             = system.executionContext
    val shutdown                                  = CoordinatedShutdown(system)

    val config        = system.settings.config
    val host: String  = config.getString("hmda.ws.host")
    val port: Int     = config.getInt("hmda.ws.port")
    val routes: Route = BaseWsApi.route(name) ~ SubmissionWsApi.routes
    Http()(classicSystem).newServerAt(host, port).bindFlow(routes).onComplete {
      case Failure(exception) =>
        system.log.error(s"Failed to start $name HTTP server, shutting down", exception)
        system.terminate()

      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "HTTP Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )

        shutdown.addTask(
          CoordinatedShutdown.PhaseServiceRequestsDone,
          "http-walletserver-graceful-terminate"
        ) { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log.info(
              s"HTTP Server $name http://{}:{}/ graceful shutdown completed",
              address.getHostString,
              address.getPort
            )
            Done
          }
        }
    }
    Behaviors.empty
  }
}
// $COVERAGE-ON$