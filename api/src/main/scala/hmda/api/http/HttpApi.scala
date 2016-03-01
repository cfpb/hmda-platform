package hmda.api.http

import java.net.InetAddress
import java.time.Instant
import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Framing }
import akka.util.ByteString
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.protocol.HmdaApiProtocol
import spray.json._

import scala.concurrent.Future

trait HttpApi extends HmdaApiProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val splitLines = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

  val routes = {
    pathSingleSlash {
      get {
        complete {
          val now = Instant.now.toString
          val host = InetAddress.getLocalHost.getHostName
          val status = Status("OK", "hmda-api", now, host)
          log.debug(status.toJson.toString)
          ToResponseMarshallable(status)
        }
      } ~
        path("upload") {
          post {
            entity(as[Multipart.FormData]) { formData =>
              val done: Future[Done] = formData.parts.mapAsync(1) {
                case b: BodyPart if b.filename.exists(_.endsWith(".dat")) =>
                  b.entity.dataBytes
                    .via(splitLines)
                    .map(_.utf8String)
                    .runForeach(line => println(line))
                //printActor ! line
                case _ => Future.successful(Done)
              }.runWith(Sink.ignore)

              onSuccess(done) { _ =>
                complete {
                  "ok!"
                }
              }
            }
          }
        }
    }
  }
}
