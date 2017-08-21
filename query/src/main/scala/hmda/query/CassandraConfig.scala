package hmda.query

import com.typesafe.config.ConfigFactory

object CassandraConfig {
  val config = ConfigFactory.load()
  val cassandraHost = config.getString("cassandra.host")
  val cassandraPort = config.getInt("cassandra.port")
  val cassandraKeyspace = config.getString("cassandra.keyspace")
  val cassandraUsername = config.getString("cassandra.username")
  val cassandraPassword = config.getString("cassandra.password")
  val numberOfRetries = config.getInt("cassandra.retries")
  val retryInterval = config.getLong("cassandra.retry-interval")
}
