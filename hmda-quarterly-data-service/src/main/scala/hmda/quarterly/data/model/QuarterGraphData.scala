package hmda.quarterly.data.model

object QuarterGraphData {
  final case class GraphCoordinate(x: String, y: String)
  final case class GraphSummary(graph: String, updated: String, coordinates: Seq[GraphCoordinate])
}

