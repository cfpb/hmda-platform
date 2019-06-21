package hmda.api.http.public

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, FlowShape}
import akka.util.{ByteString, Timeout}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source}
import hmda.api.http.model.public.{Validated, ValidatedResponse}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import hmda.util.streams.FlowUtils._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.parser.filing.lar._2018.LarCsvParser
import hmda.parser.filing.ts._2018.TsCsvParser

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
            byteSource.via(processHmdaFile).runWith(Sink.seq)
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
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            fileUpload("file") {
              case (_, byteSource) =>
                val headerSource =
                  Source.fromIterator(() =>
                    List("lineNumber|errors\n").toIterator)
                val errors = byteSource
                  .via(processHmdaFile)
                  .map(v => s"${v.lineNumber}|${v.errors}\n")
                  .map(s => ByteString(s))

                val csvF = headerSource
                  .map(s => ByteString(s))
                  .concat(errors)
                  .map(_.utf8String)
                  .runWith(Sink.seq)

                onComplete(csvF) {
                  case Success(csv) =>
                    complete(
                      ToResponseMarshallable(
                        HttpEntity(ContentTypes.`text/csv(UTF-8)`,
                                   csv.mkString("\n"))))
                  case Failure(error) =>
                    complete(ToResponseMarshallable(
                      StatusCodes.BadRequest -> error.getLocalizedMessage))
                }

              case _ =>
                complete(ToResponseMarshallable(StatusCodes.BadRequest))
            }
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

  private def processHmdaFile: Flow[ByteString, Validated, NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[Validated](2))

      bcast ~> processTsSource ~> concat.in(0)
      bcast ~> processLarSource ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })
  }

  private def processTsSource: Flow[ByteString, Validated, NotUsed] = {
    Flow[ByteString]
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
  }

  private def processLarSource: Flow[ByteString, Validated, NotUsed] = {
    Flow[ByteString]
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
  }

}
