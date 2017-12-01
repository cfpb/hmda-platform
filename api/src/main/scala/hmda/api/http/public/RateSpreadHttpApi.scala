package hmda.api.http.public

import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.server.Directives._
import hmda.api.protocol.apor.RateSpreadProtocol._

trait RateSpreadHttpApi extends HmdaCustomDirectives with ApiErrorProtocol {

  val rateSpreadRoutes =
    extractExecutionContext { executor =>
      encodeResponse {
        pathPrefix("rate-spread") {
          path("calculate") {
            complete("OK")
          }
        }
      }
    }

}
