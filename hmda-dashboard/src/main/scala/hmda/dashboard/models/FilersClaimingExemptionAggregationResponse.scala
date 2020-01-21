package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersClaimingExemptionAggregationResponse(aggregations: Seq[FilersClaimingExemption])

object FilersClaimingExemptionAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersClaimingExemptionAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersClaimingExemptionAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersClaimingExemption]]
    } yield FilersClaimingExemptionAggregationResponse(a)
}
