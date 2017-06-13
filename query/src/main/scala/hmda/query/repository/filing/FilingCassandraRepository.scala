package hmda.query.repository.filing

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSink, CassandraSource}
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.driver.core._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.query.repository.CassandraRepository

import scala.concurrent.{ExecutionContext, Future}

trait FilingCassandraRepository extends CassandraRepository[LoanApplicationRegister] {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare("")
  }

  val statementBinder = (lar: LoanApplicationRegister, statement: PreparedStatement) =>
    statement.bind(

    )

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.lar(
         |
         |)
       """.stripMargin

    session.execute(query)
  }

  override def dropTable(): ResultSet = {
    val query =
      s"""
         |DROP TABLE IF EXISTS $keyspace.lar;
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[LoanApplicationRegister, NotUsed]): Future[Done] = {
    val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.lar").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }


}
