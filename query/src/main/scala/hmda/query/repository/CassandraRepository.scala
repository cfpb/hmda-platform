package hmda.query.repository

import akka.actor.{ ActorSystem, Scheduler }
import akka.stream.ActorMaterializer
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.Source
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy
import com.datastax.driver.core.{ Cluster, ResultSet, Row, Session }
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

  val log = LoggerFactory.getLogger("CassandraRepository")

  val keyspace = "hmda_query"

  @tailrec
  private def retry[T](n: Int)(fn: => T): Try[T] = {
    log.info("*********ATTEMPTING CONNECTION TO CASSANDRA QUERY CLUSTER********")
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
  def dropTable(): ResultSet
  def insertData(source: Source[A, NotUsed]): Future[Done]
  def readData(fetchSize: Int): Future[Seq[Row]]

}
