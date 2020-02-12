package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersWithOnlyOpenEndCreditTransactions(agency_code: Int, count: Int)

object FilersWithOnlyOpenEndCreditTransactions {
  implicit val getResults: GetResult[FilersWithOnlyOpenEndCreditTransactions] = GetResult(r => FilersWithOnlyOpenEndCreditTransactions(r.<<, r.<<))

  implicit val codec: Codec[FilersWithOnlyOpenEndCreditTransactions] =
    Codec.forProduct2("Agency Code", "Count")(FilersWithOnlyOpenEndCreditTransactions.apply)(f => (f.agency_code,f.count))
}