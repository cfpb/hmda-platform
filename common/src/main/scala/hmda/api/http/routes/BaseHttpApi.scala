package hmda.api.http.routes

import java.net.InetAddress
import java.time.Instant

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.HmdaServiceStatus
import io.circe.generic.auto._

trait BaseHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  def rootPath(name: String) =
    pathSingleSlash {
      timedGet { _ =>
        complete {
          val now    = Instant.now.toString
          val host   = InetAddress.getLocalHost.getHostName
          val status = HmdaServiceStatus("OK", name, now, host)
          log.debug(status.toString)
          ToResponseMarshallable(status)
        }
      }
    }

  def routes(apiName: String) =
    encodeResponse {
      rootPath(apiName)
    }

}
