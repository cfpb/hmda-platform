package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersWithOnlyClosedEndCreditTransactionsAggregationResponse(aggregations: Seq[FilersWithOnlyClosedEndCreditTransactions])

object FilersWithOnlyClosedEndCreditTransactionsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersWithOnlyClosedEndCreditTransactionsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersWithOnlyClosedEndCreditTransactionsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersWithOnlyClosedEndCreditTransactions]]
    } yield FilersWithOnlyClosedEndCreditTransactionsAggregationResponse(a)
}
