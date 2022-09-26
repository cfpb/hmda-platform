package hmda.quarterly.data.api.dto

import akka.http.scaladsl.server.Route
import hmda.quarterly.data.api.route.lib.Verbiage.DEFAULT_DECIMAL_PRECISION

object QuarterGraphData {
  final case class GraphSeriesCoordinate(x: String, y: Float)
  final case class GraphSeriesSummary(name: String, updated: String, coordinates: Seq[GraphSeriesCoordinate])
  final case class GraphSeriesInfo(title: String, subtitle: String, series: Seq[GraphSeriesSummary],
                                   xLabel: String = "Year Quarter", yLabel: String = "Value", decimalPrecision: Int = DEFAULT_DECIMAL_PRECISION)
  case class GraphRoute(title: String, category: String, endpoint: String) {
    def route: Route = ???
  }
  case class GraphRouteInfo(overview: String, graphs: Seq[GraphRoute])
}

