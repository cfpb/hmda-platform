package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersWithOnlyOpenEndCreditTransactionsAggregationResponse(aggregations: Seq[FilersWithOnlyOpenEndCreditTransactions])

object FilersWithOnlyOpenEndCreditTransactionsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersWithOnlyOpenEndCreditTransactionsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersWithOnlyOpenEndCreditTransactionsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersWithOnlyOpenEndCreditTransactions]]
    } yield FilersWithOnlyOpenEndCreditTransactionsAggregationResponse(a)
}
