package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class OpenEndCreditLarCountByAgencyAggregationResponse(aggregations: Seq[OpenEndCreditLarCountByAgency])

object OpenEndCreditLarCountByAgencyAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[OpenEndCreditLarCountByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[OpenEndCreditLarCountByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[OpenEndCreditLarCountByAgency]]
    } yield OpenEndCreditLarCountByAgencyAggregationResponse(a)
}
