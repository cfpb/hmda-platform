package hmda.api.http.routes

import java.net.InetAddress
import java.time.Instant

import akka.Done
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.Http
import hmda.api.http.directives.HmdaTimeDirectives._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.HmdaServiceStatus
import io.circe.generic.auto._
import org.slf4j.{ Logger, LoggerFactory }
import hmda.BuildInfo
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object BaseHttpApi {
  private val log: Logger = LoggerFactory.getLogger(getClass)

  def runServer(
                 shutdown: CoordinatedShutdown,
                 name: String
               )(routes: Route, host: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContext): Unit =
    Http().newServerAt(host, port).bindFlow(routes).onComplete {
      case Failure(exception) =>
        system.log.error("Failed to start HTTP server, shutting down", exception)
        system.terminate()

      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          s"HTTP Server ({}) online at http://{}:{}/",
          name,
          address.getHostString,
          address.getPort
        )

        shutdown.addTask(
          CoordinatedShutdown.PhaseServiceRequestsDone,
          s"http-$name-graceful-terminate"
        ) { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log.info(
              "HTTP Server ({}) http://{}:{}/ graceful shutdown completed",
              name,
              address.getHostString,
              address.getPort
            )
            Done
          }
        }
    }

  def routes(apiName: String)(implicit ec: ExecutionContext): Route =
    encodeResponse(rootPath(apiName))

  private def rootPath(name: String)(implicit ec: ExecutionContext): Route =
    pathSingleSlash {
      timed {
        complete {
          val now    = Instant.now.toString
          val host   = InetAddress.getLocalHost.getHostName
          val status = HmdaServiceStatus("OK", name, now, host, BuildInfo.latestGitTag)
          log.debug(status.toString)
          status
        }
      }
    }
}