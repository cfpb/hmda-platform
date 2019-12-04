package hmda.api.http.public

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.stream.{ ActorMaterializer, FlowShape }
import akka.util.{ ByteString, Timeout }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.{ Broadcast, Concat, Flow, GraphDSL, Sink, Source }
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import io.circe.generic.auto._
import hmda.api.http.model.filing.submissions.HmdaRowParsedErrorSummary
import hmda.api.http.utils.ParserErrorUtils

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
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
            byteSource.via(processHmdaFile).runWith(Sink.seq)
          onComplete(processF) {
            case Success(parsed) =>
              val errorList = parsed.filter(_ != None)
              complete(errorList)

            case Failure(error) =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
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
                  Source.fromIterator(() => List("Row Number|Estimated ULI|Field Name|Input Value|Valid Values\n").toIterator)
                val errors = byteSource
                  .via(processHmdaFile)
                  .filter(_ != None)
                  .map(option => option match {
                    case Some(error) => error.toCsv
                    case None => ""
                  })
                  .map(s => ByteString(s))

                val csvF = headerSource
                  .map(s => ByteString(s))
                  .concat(errors)
                  .map(_.utf8String)
                  .runWith(Sink.seq)

                onComplete(csvF) {
                  case Success(csv) =>
                    complete(ToResponseMarshallable(HttpEntity(ContentTypes.`text/csv(UTF-8)`, csv.mkString("\n"))))
                  case Failure(error) =>
                    complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
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

  def hmdaFileRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("hmda") {
            parseHmdaFileRoute
          }
        }
      }
    }

  private def processHmdaFile: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast  = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[Option[HmdaRowParsedErrorSummary]](2))

      bcast ~> processTsSource ~> concat.in(0)
      bcast ~> processLarSource ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })

  private def processTsSource: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
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
        case (i, Right(_)) => None
        case (i, Left(errors)) =>
          Some(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, "ts", errors))
      }

  private def processLarSource: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .drop(1)
      .map(_.trim)
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .map {
        case (lar, index) =>
          (index, lar, LarCsvParser(lar))
      }
      .map {
        case (i, lar, Right(_)) => None
        case (i, lar, Left(errors)) =>
          Some(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, lar, errors))
      }

}
