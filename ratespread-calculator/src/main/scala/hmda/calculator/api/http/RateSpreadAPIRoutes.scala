package hmda.calculator.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{
  as,
  complete,
  encodeResponse,
  entity,
  extractUri,
  fileUpload,
  path,
  pathPrefix,
  _
}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.{ByteString, Timeout}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.calculator.api.model.RateSpreadRequest._
import hmda.calculator.apor.APORCommands
import hmda.calculator.parser.RateSpreadCSVParser
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.util.streams.FlowUtils
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

trait RateSpreadAPIRoutes extends HmdaTimeDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  val rateSpreadRoutes = encodeResponse {
    pathPrefix("v2") {
      path("rateSpread") {
        extractUri { uri =>
          entity(as[RateSpreadRequest]) { rateSpreadRequest =>
            val rateSpreadResponse =
              Try(APORCommands.getRateSpreadResponse(rateSpreadRequest))
            rateSpreadResponse match {
              case Success(response) =>
                complete(ToResponseMarshallable(response))
              case Failure(error) =>
                failedResponse(StatusCodes.BadRequest, uri, error)
            }
          }
        }
      } ~
        path("rateSpread" / "csv") {
          fileUpload("file") {
            case (_, byteSource) =>
              val headerSource =
                Source.fromIterator(() =>
                  List("action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spread\n").toIterator)
              val rateSpreadValues = processRateSpreadRow(byteSource)
                .map(rateSpread => rateSpread + "\n")
                .map(s => ByteString(s))

              val csv =
                headerSource.map(s => ByteString(s)).concat(rateSpreadValues)
              complete(HttpEntity.Chunked
                .fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
            case _ =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest))
          }
        }
    }
  }

  private def processRateSpreadRow(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(FlowUtils.framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map { rateSpreadRow =>
        val rateSpreadData = try (RateSpreadCSVParser.fromCsv((rateSpreadRow)))
        val rateSpreadResponse =
          APORCommands.getRateSpreadResponse(rateSpreadData)
        rateSpreadRow + "," + rateSpreadResponse.rateSpread
      }
  }
}
