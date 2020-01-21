package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersListWithOnlyOpenEndCreditAggregationResponse(aggregations: Seq[FilersListWithOnlyOpenEndCredit])

object FilersListWithOnlyOpenEndCreditAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersListWithOnlyOpenEndCreditAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersListWithOnlyOpenEndCreditAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersListWithOnlyOpenEndCredit]]
    } yield FilersListWithOnlyOpenEndCreditAggregationResponse(a)
}
