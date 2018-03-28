package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import hmda.api.http.model.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Sink, Source}
import hmda.api.http.model.public.{LarValidateRequest, LarValidateResponse}
import hmda.parser.filing.lar.LarCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.LarCodec._
import hmda.util.streams.FlowUtils._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait LarValidationHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  //lar/parse
  val parseLarRoute =
    path("parse") {
      timedPost { _ =>
        entity(as[LarValidateRequest]) { req =>
          LarCsvParser(req.lar) match {
            case Right(lar) =>
              complete(ToResponseMarshallable(lar))
            case Left(errors) =>
              val errorList = errors.map(e => e.errorMessage)
              complete(
                ToResponseMarshallable(
                  StatusCodes.BadRequest -> LarValidateResponse(errorList)))
          }
        }
      } ~
        fileUpload("file") {
          case (_, byteSource) =>
            val processF =
              processLarFile(byteSource).runWith(Sink.seq).map(_.zipWithIndex)
            onComplete(processF) {
              case Success(parsed) =>
                complete(ToResponseMarshallable(parsed))
              case Failure(error) =>
                complete(
                  ToResponseMarshallable(
                    StatusCodes.BadRequest -> error.getLocalizedMessage))
            }
          case _ =>
            complete(ToResponseMarshallable(StatusCodes.BadRequest))
        }
    }

  def larRoutes: Route = {
    encodeResponse {
      pathPrefix("lar") {
        parseLarRoute
      }
    }
  }

  private def processLarFile(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .map {
        case Right(_)     => ""
        case Left(errors) => errors.mkString(",")
      }
      .filterNot(s => s == "")

  }

}
