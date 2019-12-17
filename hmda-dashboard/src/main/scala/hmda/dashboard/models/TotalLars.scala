package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class TotalLars(count: Int)

object TotalLars {
  implicit val getResults: GetResult[TotalLars] = GetResult(r => TotalLars(r.<<))

  implicit val codec: Codec[TotalLars] =
    Codec.forProduct1("Total Lar Records")(TotalLars.apply)(f => (f.count))
}