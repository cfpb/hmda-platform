package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersCountOpenEndOriginationsByAgencyGraterOrEqual(agency_code:Int, count: Int)

object FilersCountOpenEndOriginationsByAgencyGraterOrEqual {
  implicit val getResults: GetResult[FilersCountOpenEndOriginationsByAgencyGraterOrEqual] = GetResult(r => FilersCountOpenEndOriginationsByAgencyGraterOrEqual(r.<<,r.<<))

  implicit val codec: Codec[FilersCountOpenEndOriginationsByAgencyGraterOrEqual] =
    Codec.forProduct2("Agency Code", "Count")(FilersCountOpenEndOriginationsByAgencyGraterOrEqual.apply)(f => (f.agency_code,f.count))
}