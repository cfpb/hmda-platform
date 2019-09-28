package hmda.api.http.routes

import java.net.InetAddress
import java.time.Instant

import hmda.api.http.directives.HmdaTimeDirectives._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.HmdaServiceStatus
import io.circe.generic.auto._
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext

object BaseHttpApi {
  private val log: Logger = LoggerFactory.getLogger(getClass)

  def routes(apiName: String)(implicit ec: ExecutionContext): Route =
    encodeResponse(rootPath(apiName))

  private def rootPath(name: String)(implicit ec: ExecutionContext): Route =
    pathSingleSlash {
      timed {
        complete {
          val now    = Instant.now.toString
          val host   = InetAddress.getLocalHost.getHostName
          val status = HmdaServiceStatus("OK", name, now, host)
          log.debug(status.toString)
          status
        }
      }
    }
}