package hmda.api.http

import java.net.InetAddress
import java.time.Instant
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.Config
import hmda.api.model.Status
import hmda.api.protocol.HmdaApiProtocol

trait HttpService extends HmdaApiProtocol {

  implicit val system: ActorSystem
  implicit val ec: ExecutionContext
  implicit val materializer: ActorMaterializer

  val log: LoggingAdapter
  def config: Config

  val routes = {
    pathSingleSlash {
      get {
        complete {
          val now = Instant.now.toString
          val host = InetAddress.getLocalHost.getHostName
          val status = Status("OK", now, host)
          log.debug(status.toJson.toString)
          ToResponseMarshallable(status)
        }
      }
    }
  }

}

