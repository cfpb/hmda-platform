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
import hmda.api.http.model.public.{
  LarValidateRequest,
  LarValidateResponse,
  Validated,
  ValidatedResponse
}
import hmda.parser.filing.lar.LarCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.LarCodec._
import hmda.parser.filing.ts.TsCsvParser
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
              processLarFile(byteSource).runWith(Sink.seq)
            onComplete(processF) {
              case Success(parsed) =>
                complete(ToResponseMarshallable(ValidatedResponse(parsed)))
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

    val tsSource = byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .take(1)
      .zip(Source.fromIterator(() => Iterator.from(1)))
      .map {
        case (ts, index) =>
          (index, TsCsvParser(ts))
      }
      .map {
        case (i, Right(_))     => Validated(i, "OK")
        case (i, Left(errors)) => Validated(i, errors.mkString(","))
      }
      .filter(x => x.errors != "OK")

    val larSource = byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .drop(1)
      .map(_.trim)
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .map {
        case (lar, index) =>
          (index, LarCsvParser(lar))
      }
      .map {
        case (i, Right(_))     => Validated(i, "OK")
        case (i, Left(errors)) => Validated(i, errors.mkString(","))
      }
      .filter(x => x.errors != "OK")

    tsSource ++ larSource

  }

}
