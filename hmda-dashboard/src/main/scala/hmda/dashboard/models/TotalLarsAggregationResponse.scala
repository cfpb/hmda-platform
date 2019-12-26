package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TotalLarsAggregationResponse(aggregations: Seq[TotalLars])

object TotalLarsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TotalLarsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TotalLarsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TotalLars]]
    } yield TotalLarsAggregationResponse(a)
}
