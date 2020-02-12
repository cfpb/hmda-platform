package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersCountClosedEndOriginationsByAgency(agency_code:Int, count: Int)

object FilersCountClosedEndOriginationsByAgency {
  implicit val getResults: GetResult[FilersCountClosedEndOriginationsByAgency] = GetResult(r => FilersCountClosedEndOriginationsByAgency(r.<<,r.<<))

  implicit val codec: Codec[FilersCountClosedEndOriginationsByAgency] =
    Codec.forProduct2("Agency Code", "Count")(FilersCountClosedEndOriginationsByAgency.apply)(f => (f.agency_code,f.count))
}