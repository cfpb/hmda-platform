package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersCountOpenEndOriginationsByAgency(agency_code:Int, count: Int)

object FilersCountOpenEndOriginationsByAgency {
  implicit val getResults: GetResult[FilersCountOpenEndOriginationsByAgency] = GetResult(r => FilersCountOpenEndOriginationsByAgency(r.<<,r.<<))

  implicit val codec: Codec[FilersCountOpenEndOriginationsByAgency] =
    Codec.forProduct2("Agency Code", "Count")(FilersCountOpenEndOriginationsByAgency.apply)(f => (f.agency_code,f.count))
}