package hmda.persistence.submission.repositories

import com.datastax.driver.core._
import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.outworkers.phantom.database.Database
import com.typesafe.config.Config
import hmda.persistence.submission.repository.DuplicateLineNumberRepository

import scala.collection.JavaConverters._

class SyntacticalDb private (override val connector: CassandraConnection) extends Database[SyntacticalDb](connector) {
  object distinctCountRepository extends SyntacticalRepository with connector.Connector
  object duplicateLineNumbersRepository extends DuplicateLineNumberRepository with connector.Connector
}

object SyntacticalDb {
  def apply(config: Config): SyntacticalDb = {
    val user = config.getString("cassandra-journal.authentication.username")
    val pass = config.getString("cassandra-journal.authentication.password")
    val contactPoints =
      config.getStringList("cassandra-journal.contact-points").asScala.toList
    val keyspace = config.getString("cassandra-journal.keyspace")

    lazy val connector: CassandraConnection =
      ContactPoints(contactPoints).withClusterBuilder { builder =>
        builder.withAuthProvider(new PlainTextAuthProvider(user, pass))
        builder
      }.keySpace(name = keyspace, autoinit = true)

    new SyntacticalDb(connector)
  }
}
