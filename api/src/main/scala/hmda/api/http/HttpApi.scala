package hmda.api.http

import java.net.InetAddress
import java.time.Instant
import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, Multipart }
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Framing }
import akka.util.ByteString
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.processing.ProcessingActor
import hmda.api.processing.ProcessingActor.StopActor
import hmda.api.protocol.HmdaApiProtocol
import spray.json._

import scala.concurrent.Future
import scala.util.{ Failure, Success }

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
      }
    } ~
      path("upload") {
        post {
          val processingActor = system.actorOf(ProcessingActor.props)
          entity(as[Multipart.FormData]) { formData =>
            val uploaded: Future[Done] = formData.parts.mapAsync(1) {
              //TODO: check Content-Type type as well?
              case b: BodyPart if b.filename.exists(_.endsWith(".txt")) =>
                b.entity.dataBytes
                  .via(splitLines)
                  .map(_.utf8String)
                  .runForeach(line => processingActor ! line)

              case _ => Future.failed(throw new Exception("File could not be uploaded"))
            }.runWith(Sink.ignore)

            onComplete(uploaded) {
              case Success(response) =>
                processingActor ! StopActor
                complete {
                  "uploaded"
                }
              case Failure(error) =>
                processingActor ! StopActor
                log.error(error.getLocalizedMessage)
                complete {
                  HttpResponse(StatusCodes.BadRequest, entity = "Invalid file format")
                }
            }
          }
        }
      }
  }
}
