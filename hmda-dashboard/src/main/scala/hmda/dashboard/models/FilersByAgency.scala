package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersByAgency(agency_code:Int, count: Int)

object FilersByAgency {
  implicit val getResults: GetResult[FilersByAgency] = GetResult(r => FilersByAgency(r.<<,r.<<))

  implicit val codec: Codec[FilersByAgency] =
    Codec.forProduct2("Agency Code", "Count")(FilersByAgency.apply)(f => (f.agency_code,f.count))
}