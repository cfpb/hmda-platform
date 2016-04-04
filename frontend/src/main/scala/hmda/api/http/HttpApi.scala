package hmda.api.http

import java.net.InetAddress
import java.time.Instant

import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Framing, Sink }
import akka.pattern.ask
import akka.util.{ ByteString, Timeout }
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.protocol.HmdaApiProtocol
import hmda.api.protocol.processing.ProcessingStatusProtocol
import hmda.model.messages.ProcessingStatusSeq
import hmda.persistence.HmdaFileRaw
import hmda.persistence.HmdaFileRaw.{ AddLine, CompleteUpload, GetStatus, Shutdown }
import spray.json._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait HttpApi extends HmdaApiProtocol with ProcessingStatusProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val splitLines = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

  val rootPath = pathSingleSlash {
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

  val uploadPath = path("upload" / Segment) { id =>
    post {
      val uploadTimestamp = Instant.now.toEpochMilli
      val hmdaRawFile = system.actorOf(HmdaFileRaw.props(id))
      entity(as[Multipart.FormData]) { formData =>
        val uploaded: Future[Done] = formData.parts.mapAsync(1) {
          //TODO: check Content-Type type as well?
          case b: BodyPart if b.filename.exists(_.endsWith(".txt")) =>
            b.entity.dataBytes
              .via(splitLines)
              .map(_.utf8String)
              .runForeach(line => hmdaRawFile ! AddLine(uploadTimestamp, line))

          case _ => Future.failed(throw new Exception("File could not be uploaded"))
        }.runWith(Sink.ignore)

        onComplete(uploaded) {
          case Success(response) =>
            hmdaRawFile ! CompleteUpload
            hmdaRawFile ! Shutdown
            complete {
              "uploaded"
            }
          case Failure(error) =>
            hmdaRawFile ! Shutdown
            log.error(error.getLocalizedMessage)
            complete {
              HttpResponse(StatusCodes.BadRequest, entity = "Invalid file format")
            }
        }
      }
    }
  }

  val processingStatusPath = path("status" / Segment) { id =>
    //TODO: make timeout configurable?
    implicit val timeout = Timeout(5.seconds)
    get {
      val processingActor = system.actorOf(HmdaFileRaw.props(id))
      onComplete((processingActor ? GetStatus).mapTo[ProcessingStatusSeq]) {
        case Success(s) =>
          processingActor ! Shutdown
          complete(ToResponseMarshallable(s))
        case Failure(e) =>
          processingActor ! Shutdown
          complete(HttpResponse(StatusCodes.InternalServerError))
      }
    }
  }

  val routes = rootPath ~ uploadPath ~ processingStatusPath
}
