package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class OpenEndCreditLarCountByAgency(agency_code:Int, count: Int)

object OpenEndCreditLarCountByAgency {
  implicit val getResults: GetResult[OpenEndCreditLarCountByAgency] = GetResult(r => OpenEndCreditLarCountByAgency(r.<<,r.<<))

  implicit val codec: Codec[OpenEndCreditLarCountByAgency] =
    Codec.forProduct2("Agency Code", "Count")(OpenEndCreditLarCountByAgency.apply)(f => (f.agency_code,f.count))
}