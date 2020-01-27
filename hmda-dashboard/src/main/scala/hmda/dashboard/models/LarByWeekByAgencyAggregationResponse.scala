package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class LarByWeekByAgencyAggregationResponse(aggregations: Seq[LarByWeekByAgency])

object LarByWeekByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[LarByWeekByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[LarByWeekByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[LarByWeekByAgency]]
    } yield LarByWeekByAgencyAggregationResponse(a)
}
