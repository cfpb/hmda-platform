package hmda.api.http

import java.net.InetAddress
import java.time.Instant
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.protocol.HmdaApiProtocol
import spray.json._

trait BaseHttpApi extends HmdaApiProtocol with HmdaCustomDirectives {

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
          log.debug(status.toJson.toString)
          ToResponseMarshallable(status)
        }
      }
    }

  def routes(apiName: String) = encodeResponse { rootPath(apiName) }
}
