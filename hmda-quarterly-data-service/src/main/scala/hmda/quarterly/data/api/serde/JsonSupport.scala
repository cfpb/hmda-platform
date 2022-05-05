package hmda.quarterly.data.api.serde

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import hmda.quarterly.data.api.dao.AggregatedVolume
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphCoordinate, GraphSummary }
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coordinateFormat: RootJsonFormat[GraphCoordinate] = jsonFormat2(GraphCoordinate)
  implicit val graphData: RootJsonFormat[GraphSummary] = jsonFormat3(GraphSummary)

  protected def convertToGraph(description: String, aggregatedVolume: Seq[AggregatedVolume]): GraphSummary = {
    val coordinates = aggregatedVolume.map(loc => GraphCoordinate(loc.quarter, loc.volume.toString))
    val updated = aggregatedVolume.head.lastUpdated
    GraphSummary(description, updated.toString, coordinates)
  }
}
