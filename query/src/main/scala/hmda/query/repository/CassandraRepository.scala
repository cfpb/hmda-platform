package hmda.query.repository

import akka.actor.{ ActorSystem, Scheduler }
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.{ Flow, Source }
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy
import com.datastax.driver.core._
import hmda.query.CassandraConfig._

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Success, Try }
import org.slf4j.LoggerFactory

trait CassandraRepository[A] {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val scheduler: Scheduler

  val repositoryLog = LoggerFactory.getLogger("CassandraRepository")

  val keyspace = cassandraKeyspace

  def table: String

  @tailrec
  private def retry[T](n: Int)(fn: => T): Try[T] = {
    repositoryLog.info("*********ATTEMPTING CONNECTION TO CASSANDRA QUERY CLUSTER********")
    Try { fn } match {
      case x: Success[T] => x
      case _ if n > 1 =>
        Thread.sleep(retryInterval)
        retry(n - 1)(fn)
      case fn => fn
    }
  }

  implicit val session: Session = {
    retry(numberOfRetries) {
      Cluster
        .builder
        .addContactPoint(cassandraHost)
        .withPort(cassandraPort)
        .withCredentials(cassandraUsername, cassandraPassword)
        .withReconnectionPolicy(new ExponentialReconnectionPolicy(100L, 200000L))
        .build
        .connect()
    }.getOrElse(null)
  }

  def createKeyspace(): ResultSet = {
    val query =
      s"""
        |CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = {
        |  'class': 'SimpleStrategy',
        |  'replication_factor': '1'
        |}
      """.stripMargin

    session.execute(query)
  }
  def dropKeyspace(): ResultSet = {
    val query =
      s"""
        |DROP KEYSPACE $keyspace
      """.stripMargin
    session.execute(query)
  }
  def createTable(): ResultSet
  def dropTable(): ResultSet = {
    val query =
      s"""
         |DROP TABLE IF EXISTS $keyspace.$table;
       """.stripMargin

    session.execute(query)
  }
  def insertData(source: Source[A, NotUsed]): Future[Done]
  def readData(fetchSize: Int): Source[A, NotUsed] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.$table").setFetchSize(fetchSize)
    val rowSource = CassandraSource(statement)
    val entitySource = rowSource.via(parseRows)
    entitySource
  }
  protected def parseRows: Flow[Row, A, NotUsed]

}
