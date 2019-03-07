package hmda.calculator.api.http

import akka.event.LoggingAdapter
import akka.http.scaladsl.common.{
  EntityStreamingSupport,
  JsonEntityStreamingSupport
}
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
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.calculator.api.model.RateSpreadModel.{
  RateSpreadCheck,
  RateSpreadValidated
}
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.calculator.api.validation.RateSpread._

import scala.util.{Failure, Success, Try}

trait RateSpreadAPIRoutes extends HmdaTimeDirectives {

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()
  val log: LoggingAdapter

  val rateSpreadRoutes =
    encodeResponse {
      pathPrefix("ratespread") {
        path("test") {
          extractUri { uri =>
            entity(as[RateSpreadCheck]) { rsc =>
              val rateSpread = rsc.rateSpread
              val isValid = Try(validateRateSpread(rateSpread))
              isValid match {
                case Success(value) =>
                  complete(ToResponseMarshallable(value))
                case Failure(error) =>
                  failedResponse(StatusCodes.BadRequest, uri, error)
              }
            }
          }
        }
      }
    }
}
