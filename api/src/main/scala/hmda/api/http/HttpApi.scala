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
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import hmda.api.protocol.HmdaApiProtocol
import spray.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait HttpApi extends HmdaApiProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val rootPath =
    pathSingleSlash {
      get {
        complete {
          val now = Instant.now.toString
          val host = InetAddress.getLocalHost.getHostName
          val status = Status("OK", "hmda-api", now, host)
          log.debug(status.toJson.toString)
          ToResponseMarshallable(status)
        }
      }
    }

  val routes = rootPath

  def beSlow(echo: String): Future[String] = {
    Future {
      Thread sleep 25000
      echo
    }
  }

}
