package hmda.persistence

import com.datastax.driver.core.QueryLogger
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

object QueryLoggerProvider extends LazyLogging {

  def queryLogger(config: Config): Option[QueryLogger] =
    if (config.getBoolean("log-queries")) {
      val keyspace                        = config.getString("keyspace")
      val slowQueryLatencyThresholdMillis = config.getLong("slow-query-latency-threshold-millis")
      logger.info(s"Cassandra slow queries enabled for $keyspace with threshold $slowQueryLatencyThresholdMillis")
      Some(QueryLogger.builder().withConstantThreshold(slowQueryLatencyThresholdMillis).build())
    } else {
      None
    }

}