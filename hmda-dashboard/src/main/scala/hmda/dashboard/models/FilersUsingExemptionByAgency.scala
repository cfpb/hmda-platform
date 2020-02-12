package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersUsingExemptionByAgency(agency_code:Int, count: Int)

object FilersUsingExemptionByAgency {
  implicit val getResults: GetResult[FilersUsingExemptionByAgency] = GetResult(r => FilersUsingExemptionByAgency(r.<<,r.<<))

  implicit val codec: Codec[FilersUsingExemptionByAgency] =
    Codec.forProduct2("Agency Code", "Count")(FilersUsingExemptionByAgency.apply)(f => (f.agency_code,f.count))
}