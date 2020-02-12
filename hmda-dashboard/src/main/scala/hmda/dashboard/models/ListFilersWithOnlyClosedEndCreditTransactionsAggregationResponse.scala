package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse(aggregations: Seq[ListFilersWithOnlyClosedEndCreditTransactions])

object ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[ListFilersWithOnlyClosedEndCreditTransactions]]
    } yield ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse(a)
}
