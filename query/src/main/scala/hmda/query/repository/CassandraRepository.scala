package hmda.query.repository

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.datastax.driver.core.{ Cluster, ResultSet, Row }
import hmda.query.CassandraConfig._
import scala.concurrent.duration._
import scala.concurrent.Future

trait CassandraRepository[A] {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val timeout = 10.seconds

  val keyspace = "hmda_query"

  implicit val session = Cluster
    .builder
    .addContactPoint(cassandraHost)
    .withPort(cassandraPort)
    .build
    .connect()

  def createKeyspace(): ResultSet = {
    val query =
      s"""
        |CREATE KEYSPACE IF NOT EXISTS $keyspace (
        |  'class': 'SimpleStrategy',
        |  'replication_factor': '1'
        |)
      """.stripMargin

    session.execute(query)
  }
  def createTable(): Unit
  def dropTable(): Unit
  def insertData(source: Source[A, NotUsed]): Future[Done]
  def readData(fetchSize: Int): Future[Seq[Row]]

}
