package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class TSRecordCount(count: Int)

object TSRecordCount {
  implicit val getResults: GetResult[TSRecordCount] = GetResult(r => TSRecordCount(r.<<))

  implicit val codec: Codec[TSRecordCount] =
    Codec.forProduct1("Total TS Records")(TSRecordCount.apply)(f => (f.count))
}