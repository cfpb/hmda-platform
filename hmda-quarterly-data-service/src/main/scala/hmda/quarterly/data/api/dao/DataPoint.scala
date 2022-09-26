package hmda.quarterly.data.api.dao

import slick.jdbc.{ GetResult, PositionedResult }

import java.sql.Timestamp

case class DataPoint (
  lastUpdated: Timestamp,
  quarter: String,
  value: Float
)
object DataPoint {
  implicit val getResult: GetResult[DataPoint] = GetResult(result => {
    val (lastUpdated, quarter, value) = getBaseData(result)
    DataPoint(lastUpdated, quarter, value)
  })
  def getBaseData(result: PositionedResult): (Timestamp, String, Float) = (
    result.rs.getTimestamp("last_updated"),
    result.rs.getString("quarter"),
    result.rs.getFloat("value")
  )
}