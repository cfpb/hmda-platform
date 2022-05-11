package hmda.quarterly.data.api.dto

object QuarterGraphData {
  final case class GraphCoordinate(x: String, y: Float)
  final case class GraphSummary(graph: String, updated: String, coordinates: Seq[GraphCoordinate])
}

