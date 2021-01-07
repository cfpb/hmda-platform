package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

case class StatisticRedis(count: Long, sum: Double, redis: Boolean)

object StatisticRedis {
  implicit val getResult: GetResult[StatisticRedis] = GetResult(r => StatisticRedis(r.<<, r.<<, r.<<))

  implicit val codec: Codec[StatisticRedis] =
    Codec.forProduct3("count", "sum", "redis")(StatisticRedis.apply)(s => (s.count, s.sum, true))
}
