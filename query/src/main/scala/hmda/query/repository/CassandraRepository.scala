package hmda.query.repository

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.datastax.driver.core.{Cluster, ResultSet}
import hmda.query.CassandraConfig._

trait CassandraRepository {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  implicit val session = Cluster
    .builder
    .addContactPoint(cassandraHost)
    .withPort(cassandraPort)
    .build
    .connect()

  def createKeyspace(): ResultSet = {
    val query =
      """
        |CREATE KEYSPACE IF NOT EXISTS hmda_query;
      """.stripMargin

    session.execute(query)
  }
  def createTable(): ResultSet
  def deleteTable(): ResultSet

}
