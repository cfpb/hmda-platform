package hmda.calculator.api.http

import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.{ HttpCharsets, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import akka.util.ByteString
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.calculator.api.model.RateSpreadRequest
import hmda.calculator.apor.APORCommands
import hmda.calculator.parser.RateSpreadCSVParser
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.util.streams.FlowUtils
import io.circe.generic.auto._
import org.slf4j.Logger

import scala.util.{ Failure, Success, Try }

object RateSpreadAPIRoutes {
  def create(log: Logger): Route = new RateSpreadAPIRoutes(log).rateSpreadRoutes
}

private class RateSpreadAPIRoutes(log: Logger) {

  val rateSpreadRoutes: Route = encodeResponse {
    handleRejections(corsRejectionHandler) {
      cors() {
        path("rateSpread") {
          (extractUri & post) { uri =>
            entity(as[RateSpreadRequest]) { rateSpreadBody =>
              val rateSpreadResponse =
                Try(APORCommands.getRateSpreadResponse(rateSpreadBody))
              rateSpreadResponse match {

                case Success(response) =>
                  log.info("API Rate Spread Request: " + rateSpreadBody.toString + "\n" + " RateSpread Result: " + response.rateSpread)
                  complete(response)

                case Failure(error) =>
                  log.info("API Rate Spread Request Failed: " + rateSpreadBody.toString + "\n" + " Error Message: " + error.toString)
                  failedResponse(BadRequest, uri, error)
              }
            }
          }
        } ~
          pathPrefix("rateSpread") {
            path("csv") {
              fileUpload("file") {
                case (_, byteSource) =>
                  val headerSource =
                    Source.fromIterator(() =>
                      List("action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spread\n").toIterator
                    )
                  val rateSpreadValues = processRateSpreadRow(byteSource)
                    .map(rateSpread => rateSpread + "\n")
                    .map(s => ByteString(s))

                  val csv =
                    headerSource
                      .map(s => ByteString(s))
                      .concat(rateSpreadValues)

                  log.info("CSV Rate Spread Request Received.")

                  complete(
                    HttpEntity.Chunked
                      .fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv)
                  )

                case _ =>
                  complete(BadRequest)
              }
            }
          }
      }
    }
  }

  private def processRateSpreadRow(byteSource: Source[ByteString, Any]) =
    byteSource
      .via(FlowUtils.framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map { rateSpreadRow =>
        val rateSpreadBody =
          try {
            RateSpreadCSVParser.fromCsv(rateSpreadRow)
          } catch {
            case error: Throwable =>
              rateSpreadRow + ", " + "error:invalid rate spread CSV :" + error.toString
          }
        rateSpreadBody match {
          case rateSpreadBody: RateSpreadRequest =>
            rateSpreadRow + "," + APORCommands
              .getRateSpreadResponse(rateSpreadBody)
              .rateSpread

          case errorMessage => errorMessage
        }
      }
}