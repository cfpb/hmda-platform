package hmda.query.repository.filing

import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.datastax.driver.core._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository

import scala.concurrent.Future

trait FilingCassandraRepository extends CassandraRepository[LoanApplicationRegisterQuery] with ProjectionRuntime {

  override val table = "lars2017"

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.$table" +
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
         |CREATE TABLE IF NOT EXISTS $keyspace.$table(
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
         |DROP TABLE IF EXISTS $keyspace.$table;
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[LoanApplicationRegisterQuery, NotUsed]): Future[Done] = {
    val sink = CassandraSink[LoanApplicationRegisterQuery](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override protected def parseRows: Flow[Row, LoanApplicationRegisterQuery, NotUsed] = {
    Flow[Row].map { row =>
      val id = row.getString("id")
      val respId = row.getString("respondent_id")
      val agencyCode = row.getInt("agency_code")
      val loanId = row.getString("loan_id")
      val applicatioDate = row.getString("application_date")
      val loanType = row.getInt("loan_type")
      val propertyType = row.getInt("property_type")
      val purpose = row.getInt("purpose")
      val occupancy = row.getInt("occupancy")
      val amount = row.getInt("amount")
      val preapprovals = row.getInt("preapprovals")
      val actionTakenType = row.getInt("action_taken_type")
      val actionTakenDate = row.getInt("action_taken_date")
      val msa = row.getString("msa")
      val state = row.getString("state")
      val county = row.getString("county")
      val tract = row.getString("tract")
      val ethnicity = row.getInt("ethnicity")
      val coEthnicity = row.getInt("co_ethnicity")
      val race1 = row.getInt("race1")
      val race2 = row.getString("race2")
      val race3 = row.getString("race3")
      val race4 = row.getString("race4")
      val race5 = row.getString("race5")
      val coRace1 = row.getInt("co_race1")
      val coRace2 = row.getString("co_race2")
      val coRace3 = row.getString("co_race3")
      val coRace4 = row.getString("co_race4")
      val coRace5 = row.getString("co_race5")
      val sex = row.getInt("sex")
      val coSex = row.getInt("co_sex")
      val income = row.getString("income")
      val purchaserType = row.getInt("purchaser_type")
      val denial1 = row.getString("denial1")
      val denial2 = row.getString("denial2")
      val denial3 = row.getString("denial3")
      val rateSpread = row.getString("rate_spread")
      val hoepaStatus = row.getInt("hoepa_status")
      val lienStatus = row.getInt("lien_status")

      LoanApplicationRegisterQuery(
        id,
        respId,
        agencyCode,
        preapprovals,
        actionTakenType,
        actionTakenDate,
        purchaserType,
        rateSpread,
        hoepaStatus,
        lienStatus,
        loanId,
        applicatioDate,
        loanType,
        propertyType,
        purpose,
        occupancy,
        amount,
        msa,
        state,
        county,
        tract,
        ethnicity,
        coEthnicity,
        race1,
        race2,
        race3,
        race4,
        race5,
        coRace1,
        coRace2,
        coRace3,
        coRace4,
        coRace5,
        sex,
        coSex,
        income,
        denial1,
        denial2,
        denial3,
        "",
        "2017"
      )
    }
  }

}
