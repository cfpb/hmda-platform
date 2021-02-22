package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersUsingExemptionByAgencyAggregationResponse(aggregations: Seq[FilersUsingExemptionByAgency])

object FilersUsingExemptionByAgencyAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[FilersUsingExemptionByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersUsingExemptionByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersUsingExemptionByAgency]]
    } yield FilersUsingExemptionByAgencyAggregationResponse(a)
}
