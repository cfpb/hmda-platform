package hmda.query.repository.filing

import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Sink, Source }
import com.datastax.driver.core._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository

import scala.concurrent.Future

trait FilingCassandraRepository extends CassandraRepository[LoanApplicationRegisterQuery] with ProjectionRuntime {

  val larTable = "lars2017"

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.$larTable" +
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

  val statementBinder = (lar: LoanApplicationRegisterQuery, statement: PreparedStatement) =>
    statement.bind(
      lar.id,
      lar.respondentId,
      new Integer(lar.agencyCode),
      lar.loanId,
      lar.applicationDate,
      new Integer(lar.loanType),
      new Integer(lar.propertyType),
      new Integer(lar.purpose),
      new Integer(lar.occupancy),
      new Integer(lar.amount),
      new Integer(lar.preapprovals),
      new Integer(lar.actionTakenType),
      new Integer(lar.actionTakenDate),
      lar.msa,
      lar.state,
      lar.county,
      lar.tract,
      new Integer(lar.ethnicity),
      new Integer(lar.coEthnicity),
      new Integer(lar.race1),
      lar.race2,
      lar.race3,
      lar.race4,
      lar.race5,
      new Integer(lar.coRace1),
      lar.coRace2,
      lar.coRace3,
      lar.coRace4,
      lar.coRace5,
      new Integer(lar.sex),
      new Integer(lar.coSex),
      lar.income,
      new Integer(lar.purchaserType),
      lar.denialReason1,
      lar.denialReason2,
      lar.denialReason3,
      lar.rateSpread,
      new Integer(lar.hoepaStatus),
      new Integer(lar.lienStatus)
    )

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.$larTable(
         |      id varchar PRIMARY KEY,
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
         |DROP TABLE IF EXISTS $keyspace.$larTable;
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[LoanApplicationRegisterQuery, NotUsed]): Future[Done] = {
    val sink = CassandraSink[LoanApplicationRegisterQuery](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.$larTable").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
