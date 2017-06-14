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
    session.prepare(s"INSERT INTO $keyspace.lars2017" +
      s"(id," +
      s"respondent_id," +
      s"agency_code," +
      s"loan_id," +
      s"application_date" +
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
    statement.bind(
      new Integer(lar.id),
      lar.respondentId,
      new Integer(lar.agencyCode),
      lar.loan.id,
      lar.loan.applicationDate,
      new Integer(lar.loan.loanType),
      new Integer(lar.loan.propertyType),
      new Integer(lar.loan.purpose),
      new Integer(lar.loan.occupancy),
      new Integer(lar.loan.amount),
      new Integer(lar.preapprovals),
      new Integer(lar.actionTakenType),
      new Integer(lar.actionTakenDate),
      lar.geography.msa,
      lar.geography.state,
      lar.geography.county,
      lar.geography.tract,
      new Integer(lar.applicant.ethnicity),
      new Integer(lar.applicant.coEthnicity),
      new Integer(lar.applicant.race1),
      lar.applicant.race2,
      lar.applicant.race3,
      lar.applicant.race4,
      lar.applicant.race5,
      new Integer(lar.applicant.coRace1),
      lar.applicant.coRace2,
      lar.applicant.coRace3,
      lar.applicant.coRace4,
      lar.applicant.coRace5,
      new Integer(lar.applicant.sex),
      new Integer(lar.applicant.coSex),
      lar.applicant.income,
      new Integer(lar.purchaserType),
      lar.rateSpread,
      new Integer(lar.hoepaStatus),
      new Integer(lar.lienStatus)
    )

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.lars2017(
         |      id int PRIMARY KEY,
         |      respondent_id varchar,
         |      agency_code int,
         |      loan_id varchar,
         |      application_date varchar,
         |      loan_type int,
         |      property_type int,
         |      purpose int,
         |      occupancy int,
         |      amount int,
         |      preapprovals int,
         |      action_taken_type int,
         |      action_taken_date int,
         |      msa varchar,
         |      state varchar,
         |      county varchar,
         |      tract varchar,
         |      ethnicity int,
         |      co_ethnicity int,
         |      race1 int,
         |      race2 varchar,
         |      race3 varchar,
         |      race4 varchar,
         |      race5 varchar,
         |      co_race1 int,
         |      co_race2 varchar,
         |      co_race3 varchar,
         |      co_race4 varchar,
         |      co_race5 varchar,
         |      sex int,
         |      co_sex int,
         |      income varchar,
         |      purchaser_type int,
         |      denial1 varchar,
         |      denial2 varchar,
         |      denial3 varchar,
         |      rate_spread varchar,
         |      hoepa_status int,
         |      lien_status int
         |);
       """.stripMargin

    session.execute(query)
  }

  override def dropTable(): ResultSet = {
    val query =
      s"""
         |DROP TABLE IF EXISTS $keyspace.lars2017;
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[LoanApplicationRegister, NotUsed]): Future[Done] = {
    val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.lars2017").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
