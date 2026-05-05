package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

case class Statistic(count: Long, sum: Double)

object Statistic {
  implicit val getResult: GetResult[Statistic] = GetResult(r => Statistic(r.<<, r.<<))

  implicit val codec: Codec[Statistic] =
    Codec.forProduct2("count", "sum")(Statistic.apply)(s => (s.count, s.sum))
}
