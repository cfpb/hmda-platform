package hmda.quarterly.data.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import hmda.quarterly.data.api.route._

object HmdaQuarterlyDataRequestHandler {

  val routes: Route = {
    pathPrefix("graph") {
      LineOfCredit.routes ~ NonStandardLoan.routes
    }
  }
}
