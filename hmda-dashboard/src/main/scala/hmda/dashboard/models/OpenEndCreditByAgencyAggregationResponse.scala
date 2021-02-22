package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class OpenEndCreditByAgencyAggregationResponse(aggregations: Seq[OpenEndCreditByAgency])

object OpenEndCreditByAgencyAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[OpenEndCreditByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[OpenEndCreditByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[OpenEndCreditByAgency]]
    } yield OpenEndCreditByAgencyAggregationResponse(a)
}
