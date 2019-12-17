package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class LARByAgency(agency_code: Int, count: Int)

object LARByAgency {
  implicit val getResults: GetResult[LARByAgency] = GetResult(r => LARByAgency(r.<<, r.<<))

  implicit val codec: Codec[LARByAgency] =
    Codec.forProduct2("Agency Code", "Count")(LARByAgency.apply)(f => (f.agency_code,f.count))
}