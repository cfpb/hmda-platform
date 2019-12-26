package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TSRecordCountAggregationResponse(aggregations: Seq[TSRecordCount])

object TSRecordCountAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TSRecordCountAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TSRecordCountAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TSRecordCount]]
    } yield TSRecordCountAggregationResponse(a)
}
