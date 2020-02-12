package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersByWeekByAgency(agency_code:Int, count: Int)

object FilersByWeekByAgency {
  implicit val getResults: GetResult[FilersByWeekByAgency] = GetResult(r => FilersByWeekByAgency(r.<<,r.<<))

  implicit val codec: Codec[FilersByWeekByAgency] =
    Codec.forProduct2("Agency Code", "Count")(FilersByWeekByAgency.apply)(f => (f.agency_code,f.count))
}