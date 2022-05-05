package hmda.quarterly.data.api.dao
import hmda.model.filing.lar.enums.LineOfCreditEnum
import slick.jdbc.GetResult

import java.sql.Timestamp

case class LineOfCreditVolume(
  lastUpdated: Timestamp,
  quarter: String,
  volume: Long,
  lineOfCreditCode: LineOfCreditEnum) extends AggregatedVolume
object LineOfCreditVolume {
  implicit val getResults: GetResult[LineOfCreditVolume] = GetResult(result => LineOfCreditVolume(
      result.rs.getTimestamp("last_updated"),
      result.rs.getString("quarter"),
      result.rs.getLong("agg"),
      LineOfCreditEnum.valueOf(result.rs.getInt("line_of_credits")))
  )
}
