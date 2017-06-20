package hmda.query.repository.filing

import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Sink, Source }
import com.datastax.driver.core._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository

import scala.concurrent.Future

trait FilingCassandraRepository extends CassandraRepository[LoanApplicationRegister] with ProjectionRuntime {

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.lars2017" +
      "(id," +
      "respondent_id," +
      "agency_code," +
      "loan_id," +
      "application_date," +
      "loan_type," +
      "property_type," +
      "purpose," +
      "occupancy," +
      "amount," +
      "preapprovals," +
      "action_taken_type," +
      "action_taken_date," +
      "msa," +
      "state," +
      "county," +
      "tract," +
      "ethnicity," +
      "co_ethnicity," +
      "race1," +
      "race2," +
      "race3," +
      "race4," +
      "race5," +
      "co_race1," +
      "co_race2," +
      "co_race3," +
      "co_race4," +
      "co_race5," +
      "sex," +
      "co_sex," +
      "income," +
      "purchaser_type," +
      "denial1," +
      "denial2," +
      "denial3," +
      "rate_spread," +
      "hoepa_status," +
      "lien_status)" +
      " VALUES " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
      lar.denial.reason1,
      lar.denial.reason2,
      lar.denial.reason3,
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
