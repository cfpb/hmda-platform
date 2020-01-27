package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class LarByWeekByAgency(agency_code:Int, count: Int)

object LarByWeekByAgency {
  implicit val getResults: GetResult[LarByWeekByAgency] = GetResult(r => LarByWeekByAgency(r.<<,r.<<))

  implicit val codec: Codec[LarByWeekByAgency] =
    Codec.forProduct2("Agency Code", "Count")(LarByWeekByAgency.apply)(f => (f.agency_code,f.count))
}