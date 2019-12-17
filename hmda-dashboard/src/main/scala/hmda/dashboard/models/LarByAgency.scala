package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class LarByAgency(agency_code: Int, count: Int)

object LarByAgency {
  implicit val getResults: GetResult[LarByAgency] = GetResult(r => LarByAgency(r.<<, r.<<))

  implicit val codec: Codec[LarByAgency] =
    Codec.forProduct2("Agency Code", "Count")(LarByAgency.apply)(f => (f.agency_code,f.count))
}