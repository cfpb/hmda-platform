package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersByWeekByAgencyAggregationResponse(aggregations: Seq[FilersByWeekByAgency])

object FilersByWeekByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersByWeekByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersByWeekByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersByWeekByAgency]]
    } yield FilersByWeekByAgencyAggregationResponse(a)
}
