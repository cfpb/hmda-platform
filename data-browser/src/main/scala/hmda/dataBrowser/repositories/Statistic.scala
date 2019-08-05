package hmda.dataBrowser.repositories

import slick.jdbc.GetResult

case class Statistic(count: Long, sum: Double)

object Statistic {
  implicit val getResult: GetResult[Statistic] = GetResult(
    r => Statistic(r.<<, r.<<))
}
