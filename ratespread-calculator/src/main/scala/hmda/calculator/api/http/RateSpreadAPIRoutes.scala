package hmda.calculator.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{
  as,
  complete,
  encodeResponse,
  entity,
  extractUri,
  path,
  pathPrefix
}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.calculator.api.CalculatedRateSpreadModel
import hmda.calculator.api.model.RateSpreadModel
import hmda.calculator.api.model.RateSpreadModel.RateSpread
import hmda.calculator.api.validation.RateSpread._
import hmda.util.http.FilingResponseUtils.failedResponse
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
          entity(as[RateSpread]) { rateSpread =>
            val calculatedRateSpread = Try(rateSpreadCalculation(rateSpread))
            calculatedRateSpread match {
              case Success(rateSpread) =>
                complete(ToResponseMarshallable(rateSpread))
              case Failure(error) =>
                failedResponse(StatusCodes.BadRequest, uri, error)
            }
          }
        }
      }
    }
  }
}
