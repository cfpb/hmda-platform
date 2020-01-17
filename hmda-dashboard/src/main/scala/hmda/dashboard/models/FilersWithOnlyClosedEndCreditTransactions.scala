package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersWithOnlyClosedEndCreditTransactions(agency_code: Int, count: Int)

object FilersWithOnlyClosedEndCreditTransactions {
  implicit val getResults: GetResult[FilersWithOnlyClosedEndCreditTransactions] = GetResult(r => FilersWithOnlyClosedEndCreditTransactions(r.<<, r.<<))

  implicit val codec: Codec[FilersWithOnlyClosedEndCreditTransactions] =
    Codec.forProduct2("Agency Code", "Count")(FilersWithOnlyClosedEndCreditTransactions.apply)(f => (f.agency_code,f.count))
}