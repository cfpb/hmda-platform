package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Sink, Source}
import hmda.api.http.model.public.{Validated, ValidatedResponse}
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import hmda.util.streams.FlowUtils._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

trait HmdaFileValidationHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  //hmda/parse
  val parseHmdaFileRoute =
    path("parse") {
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
      } ~
        timedOptions { _ =>
          complete("OPTIONS")
        }
    } ~
      path("parse" / "csv") {
        timedPost { _ =>
          fileUpload("file") {
            case (_, byteSource) =>
              val headerSource =
                Source.fromIterator(() =>
                  List("lineNumber|errors\n").toIterator)
              val errors = processLarFile(byteSource)
                .map(v => s"${v.lineNumber}|${v.errors}\n")
                .map(s => ByteString(s))

              val csv = headerSource.map(s => ByteString(s)).concat(errors)
              complete(HttpEntity.Chunked
                .fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
            case _ =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest))
          }
        } ~
          timedOptions { _ =>
            complete("OPTIONS")
          }
      }

  def hmdaFileRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("hmda") {
            parseHmdaFileRoute
          }
        }
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
        case (i, Right(_)) => Validated(i, "OK")
        case (i, Left(errors)) =>
          Validated(i, errors.map(e => e.errorMessage).mkString(","))
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
        case (i, Right(_)) => Validated(i, "OK")
        case (i, Left(errors)) =>
          Validated(i, errors.map(e => e.errorMessage).mkString(","))
      }
      .filter(x => x.errors != "OK")

    tsSource ++ larSource

  }

}
