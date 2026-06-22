package hmda.quarterly.data.api

import pekko.http.scaladsl.server.Directives._
import pekko.http.scaladsl.server._
import hmda.quarterly.data.api.route._
import hmda.quarterly.data.api.serde.JsonSupport

object HmdaQuarterlyGraphRequestHandler extends JsonSupport {
  val routes: Route = {
    val baseRoute = path("") {
      complete(graphRoutes)
    }
    val allRoutes = graphRoutes.graphs.map(_.route) :+ baseRoute
    ignoreTrailingSlash {
      pathPrefix("graphs") {
        concat(allRoutes:_*)
      }
    }
  }
}
