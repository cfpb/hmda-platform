package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class ListFilersWithOnlyClosedEndCreditTransactions(agency_code:Int,
                                                         institution_name: String,
                                                         lei: String )

object ListFilersWithOnlyClosedEndCreditTransactions {
  implicit val getResults: GetResult[ListFilersWithOnlyClosedEndCreditTransactions] = GetResult(r => ListFilersWithOnlyClosedEndCreditTransactions(r.<<,r.<<,r.<<))

  implicit val codec: Codec[ListFilersWithOnlyClosedEndCreditTransactions] =
    Codec.forProduct3("Agency Code", "Institution Name", "LEI")(ListFilersWithOnlyClosedEndCreditTransactions.apply)(f => (f.agency_code,f.institution_name,f.lei))
}