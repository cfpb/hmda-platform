package hmda.quarterly.data.api.dao

import slick.jdbc.PositionedResult

import java.sql.Timestamp

trait AggregatedVolume {
  val lastUpdated: Timestamp
  val quarter: String
  val volume: Float
}
object AggregatedVolume {
  def getBaseData(result: PositionedResult): (Timestamp, String, Float) = (
    result.rs.getTimestamp("last_updated"),
    result.rs.getString("quarter"),
    result.rs.getFloat("agg")
  )
}
