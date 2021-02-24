package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class LarCountUsingExemptionByAgencyAggregationResponse(aggregations: Seq[LarCountUsingExemptionByAgency])

object LarCountUsingExemptionByAgencyAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[LarCountUsingExemptionByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[LarCountUsingExemptionByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[LarCountUsingExemptionByAgency]]
    } yield LarCountUsingExemptionByAgencyAggregationResponse(a)
}
