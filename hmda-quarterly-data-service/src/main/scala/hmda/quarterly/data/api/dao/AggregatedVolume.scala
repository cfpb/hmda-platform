package hmda.quarterly.data.api.dao

import java.sql.Timestamp

trait AggregatedVolume {
  val lastUpdated: Timestamp
  val quarter: String
  val volume: Float
}
