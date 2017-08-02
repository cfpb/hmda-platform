package hmda.query.repository.institutions

import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{ Flow, Source }
import com.datastax.driver.core._
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository

import scala.concurrent.Future

trait InstitutionCassandraRepository extends CassandraRepository[InstitutionQuery] with ProjectionRuntime {

  override val table = "institutions"

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.$table" +
      "(id," +
      "agency," +
      "period," +
      "activity_year," +
      "respondent_id," +
      "type," +
      "cra," +
      "email_1," +
      "email_2," +
      "email_3," +
      "respondent_name," +
      "respondent_state," +
      "respondent_city," +
      "respondent_fips," +
      "hmda_filer," +
      "parent_respondent_id," +
      "parent_id_rssd," +
      "parent_name," +
      "parent_city," +
      "parent_state," +
      "assets," +
      "other_lender_codes," +
      "top_holder_id_rssd," +
      "top_holder_name," +
      "top_holder_city," +
      "top_holder_state," +
      "top_holder_country) " +
      " VALUES " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
  }

  val statementBinder = (institution: InstitutionQuery, statement: PreparedStatement) =>
    statement.bind(
      institution.id,
      new Integer(institution.agency),
      new Integer(institution.filingPeriod),
      new Integer(institution.activityYear),
      institution.respondentId,
      institution.institutionType,
      new java.lang.Boolean(institution.cra),
      institution.emailDomain1,
      institution.emailDomain2,
      institution.emailDomain3,
      institution.respondentName,
      institution.respondentState,
      institution.respondentCity,
      institution.respondentFipsStateNumber,
      new java.lang.Boolean(institution.hmdaFilerFlag),
      institution.parentRespondentId,
      new Integer(institution.parentIdRssd),
      institution.parentName,
      institution.parentCity,
      institution.parentState,
      new Integer(institution.assets),
      new Integer(institution.otherLenderCode),
      new Integer(institution.topHolderIdRssd),
      institution.topHolderName,
      institution.topHolderCity,
      institution.topHolderState,
      institution.topHolderCountry
    )

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.$table(
         |    id varchar PRIMARY KEY,
         |    agency int,
         |    period int,
         |    activity_year int,
         |    respondent_id varchar,
         |    type varchar,
         |    cra boolean,
         |    email_1 varchar,
         |    email_2 varchar,
         |    email_3 varchar,
         |    respondent_name varchar,
         |    respondent_state varchar,
         |    respondent_city varchar,
         |    respondent_fips varchar,
         |    hmda_filer boolean,
         |    parent_respondent_id varchar,
         |    parent_id_rssd int,
         |    parent_name varchar,
         |    parent_city varchar,
         |    parent_state varchar,
         |    assets int,
         |    other_lender_codes int,
         |    top_holder_id_rssd int,
         |    top_holder_name varchar,
         |    top_holder_city varchar,
         |    top_holder_state varchar,
         |    top_holder_country varchar
         |);
      """.stripMargin

    session.execute(query)

  }

  override def dropTable(): ResultSet = {
    val query = s"""
      |DROP TABLE IF EXISTS $keyspace.$table;
    """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[InstitutionQuery, NotUsed]): Future[Done] = {
    val sink = CassandraSink[InstitutionQuery](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override protected def parseRows: Flow[Row, InstitutionQuery, NotUsed] = {
    Flow[Row].map { row =>
      val id = row.getString("id")
      val agency = row.getInt("agency")
      val period = row.getInt("period")
      val activityYear = row.getInt("activity_year")
      val respondentId = row.getString("respondent_id")
      val institutionType = row.getString("type")
      val cra = row.getBool("cra")
      val email1 = row.getString("email_1")
      val email2 = row.getString("email_2")
      val email3 = row.getString("email_3")
      val respondentName = row.getString("respondent_name")
      val respondentState = row.getString("respondent_state")
      val respondentCity = row.getString("respondent_city")
      val respontentFips = row.getString("respondent_fips")
      val hmdaFiler = row.getBool("hmda_filer")
      val parentRespondentId = row.getString("parent_respondent_id")
      val parentIdRssd = row.getInt("parent_id_rssd")
      val parentName = row.getString("parent_name")
      val parentCity = row.getString("parent_city")
      val parentState = row.getString("parent_state")
      val assets = row.getInt("assets")
      val otherLenderCodes = row.getInt("other_lender_codes")
      val topHolderIdRssd = row.getInt("top_holder_id_rssd")
      val topHolderName = row.getString("top_holder_name")
      val topHolderCity = row.getString("top_holder_city")
      val topHolderState = row.getString("top_holder_state")
      val topHolderCountry = row.getString("top_holder_country")

      InstitutionQuery(
        id,
        agency,
        period,
        activityYear,
        respondentId,
        institutionType,
        cra,
        email1,
        email2,
        email3,
        respondentName,
        respondentState,
        respondentCity,
        respontentFips,
        hmdaFiler,
        parentRespondentId,
        parentIdRssd,
        parentName,
        parentCity,
        parentState,
        assets,
        otherLenderCodes,
        topHolderIdRssd,
        topHolderName,
        topHolderCity,
        topHolderState,
        topHolderCountry
      )

    }
  }

}
