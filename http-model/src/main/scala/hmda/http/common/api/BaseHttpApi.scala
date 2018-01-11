package hmda.http.common.api

import java.net.InetAddress
import java.time.Instant

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.http.common.directives.HmdaTimeDirectives
import hmda.http.common.model.Status

trait BaseHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  def rootPath(name: String) =
    pathSingleSlash {
      timedGet { _ =>
        complete {
          val now = Instant.now.toString
          val host = InetAddress.getLocalHost.getHostName
          val status = Status("OK", name, now, host)
          log.debug(status.toString)
          ToResponseMarshallable(status)
        }
      }
    }

  def routes(apiName: String) = encodeResponse { rootPath(apiName) }
}
