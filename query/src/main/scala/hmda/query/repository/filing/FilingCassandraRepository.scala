package hmda.query.repository.filing

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Sink, Source }
import com.datastax.driver.core._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.query.repository.CassandraRepository

import scala.concurrent.{ ExecutionContext, Future }

trait FilingCassandraRepository extends CassandraRepository[LoanApplicationRegister] {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.lars" +
      s"(id," +
      s"respondent_id," +
      s"agency_code," +
      s"loan_id," +
      s"application_type," +
      s"loan_type," +
      s"property_type," +
      s"purpose," +
      s"occupancy," +
      s"amount," +
      s"preapprovals," +
      s"action_taken_type," +
      s"action_taken_date," +
      s"msa," +
      s"state," +
      s"county," +
      s"tract," +
      s"ethnicity," +
      s"co_ethnicity," +
      s"race1," +
      s"race2," +
      s"race3," +
      s"race4," +
      s"race5," +
      s"co_race1," +
      s"co_race2," +
      s"co_race3," +
      s"co_race4," +
      s"co_race5" +
      s"sex," +
      s"co_sex," +
      s"income," +
      s"purchaser_type," +
      s"denial1," +
      s"denial2," +
      s"denial3," +
      s"rate_spread," +
      s"hoepa_status," +
      s"lien_status) " +
      s" VALUES " +
      s"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,)")
  }

  val statementBinder = (lar: LoanApplicationRegister, statement: PreparedStatement) =>
    statement.bind()

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.lars(
         |
         |)
       """.stripMargin

    session.execute(query)
  }

  override def dropTable(): ResultSet = {
    val query =
      s"""
         |DROP TABLE IF EXISTS $keyspace.lars;
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[LoanApplicationRegister, NotUsed]): Future[Done] = {
    val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.lars").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
