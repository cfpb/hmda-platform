package hmda.quarterly.data.api.serde

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import hmda.quarterly.data.api.dao.DataPoint
import hmda.quarterly.data.api.dto.QuarterGraphData._
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val seriesCoordinateFormat: RootJsonFormat[GraphSeriesCoordinate] = jsonFormat2(GraphSeriesCoordinate)
  implicit val seriesSummaryFormat: RootJsonFormat[GraphSeriesSummary] = jsonFormat3(GraphSeriesSummary)
  implicit val seriesInfo: RootJsonFormat[GraphSeriesInfo] = jsonFormat6(GraphSeriesInfo)
  implicit val routeFormat: RootJsonFormat[GraphRoute] = jsonFormat3(GraphRoute)
  implicit val routeInfoFormat: RootJsonFormat[GraphRouteInfo] = jsonFormat2(GraphRouteInfo)

  protected def convertToGraph(description: String, aggregatedVolume: Seq[DataPoint]): GraphSeriesSummary = {
    if (aggregatedVolume.nonEmpty) {
      val coordinates = aggregatedVolume.map(aggregatedRecord => GraphSeriesCoordinate(aggregatedRecord.quarter, aggregatedRecord.value))
      val updated = aggregatedVolume.head.lastUpdated
      GraphSeriesSummary(description, updated.toString, coordinates)
    } else {
      GraphSeriesSummary(description, "", Seq())
    }
  }
}
