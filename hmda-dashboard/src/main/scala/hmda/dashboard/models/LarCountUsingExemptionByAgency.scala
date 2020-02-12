package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class LarCountUsingExemptionByAgency(agency_code:Int, count: Int)

object LarCountUsingExemptionByAgency {
  implicit val getResults: GetResult[LarCountUsingExemptionByAgency] = GetResult(r => LarCountUsingExemptionByAgency(r.<<,r.<<))

  implicit val codec: Codec[LarCountUsingExemptionByAgency] =
    Codec.forProduct2("Agency Code", "Count")(LarCountUsingExemptionByAgency.apply)(f => (f.agency_code,f.count))
}