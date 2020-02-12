package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class FilersCountClosedEndOriginationsByAgencyGraterOrEqual(agency_code:Int, count: Int)

object FilersCountClosedEndOriginationsByAgencyGraterOrEqual {
  implicit val getResults: GetResult[FilersCountClosedEndOriginationsByAgencyGraterOrEqual] = GetResult(r => FilersCountClosedEndOriginationsByAgencyGraterOrEqual(r.<<,r.<<))

  implicit val codec: Codec[FilersCountClosedEndOriginationsByAgencyGraterOrEqual] =
    Codec.forProduct2("Agency Code", "Count")(FilersCountClosedEndOriginationsByAgencyGraterOrEqual.apply)(f => (f.agency_code,f.count))
}