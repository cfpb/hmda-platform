package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TopInstitutionsCountOpenEndCreditAggregationResponse(aggregations: Seq[TopInstitutionsCountOpenEndCredit])

object TopInstitutionsCountOpenEndCreditAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TopInstitutionsCountOpenEndCreditAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TopInstitutionsCountOpenEndCreditAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TopInstitutionsCountOpenEndCredit]]
    } yield TopInstitutionsCountOpenEndCreditAggregationResponse(a)
}
