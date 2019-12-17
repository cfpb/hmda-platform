package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class LarByAgencyAggregationResponse(aggregations: Seq[LarByAgency])

object LarByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[LarByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[LarByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[LarByAgency]]
    } yield LarByAgencyAggregationResponse(a)
}
