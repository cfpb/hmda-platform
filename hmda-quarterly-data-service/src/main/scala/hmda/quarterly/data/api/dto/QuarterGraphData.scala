package hmda.quarterly.data.api.dto

import akka.http.scaladsl.server.Route

object QuarterGraphData {
  final case class GraphSeriesCoordinate(x: String, y: Float)
  final case class GraphSeriesSummary(name: String, updated: String, coordinates: Seq[GraphSeriesCoordinate])
  final case class GraphSeriesInfo(title: String, subtitle: String, series: Seq[GraphSeriesSummary],
                                   xLabel: String = "Year Quarter", yLabel: String = "Value")
  case class GraphRoute(title: String, category: String, endpoint: String) {
    def route: Route = ???
  }
  case class GraphRouteInfo(overview: String, graphs: Seq[GraphRoute])
}

