package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class OpenEndCreditByAgency(agency_code:Int, count: Int)

object OpenEndCreditByAgency {
  implicit val getResults: GetResult[OpenEndCreditByAgency] = GetResult(r => OpenEndCreditByAgency(r.<<,r.<<))

  implicit val codec: Codec[OpenEndCreditByAgency] =
    Codec.forProduct2("Agency Code", "Count")(OpenEndCreditByAgency.apply)(f => (f.agency_code,f.count))
}