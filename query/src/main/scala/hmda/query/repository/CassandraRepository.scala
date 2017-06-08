package hmda.query.repository

import akka.{ Done, NotUsed }
import akka.stream.scaladsl.Source
import com.datastax.driver.core.policies.{ ConstantReconnectionPolicy, DowngradingConsistencyRetryPolicy, ExponentialReconnectionPolicy, LoggingRetryPolicy }
import com.datastax.driver.core.{ Cluster, ResultSet, Row, Session }
import hmda.query.CassandraConfig._

import scala.concurrent.Future

trait CassandraRepository[A] {

  val keyspace = "hmda_query"

  implicit def getSession: Session =
    try {
      Cluster
        .builder
        .addContactPoint(cassandraHost)
        .withPort(cassandraPort)
        .withReconnectionPolicy(new ExponentialReconnectionPolicy(100L, 200000L))
        .build
        .connect()
    } catch {
      case ex: Exception => getSession
    }

  def createKeyspace(): ResultSet = {
    val query =
      s"""
        |CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = {
        |  'class': 'SimpleStrategy',
        |  'replication_factor': '1'
        |}
      """.stripMargin

    getSession.execute(query)
  }
  def dropKeyspace(): ResultSet = {
    val query =
      s"""
        |DROP KEYSPACE $keyspace
      """.stripMargin
    getSession.execute(query)
  }
  def createTable(): ResultSet
  def dropTable(): ResultSet
  def insertData(source: Source[A, NotUsed]): Future[Done]
  def readData(fetchSize: Int): Future[Seq[Row]]

}
