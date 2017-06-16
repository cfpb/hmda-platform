package hmda.query.repository.institutions

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{ Sink, Source }
import com.datastax.driver.core._
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.CassandraRepository

import scala.concurrent.{ ExecutionContext, Future }

trait InstitutionCassandraRepository extends CassandraRepository[InstitutionQuery] {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.institutions" +
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
         |CREATE TABLE IF NOT EXISTS $keyspace.institutions(
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
      |DROP TABLE IF EXISTS $keyspace.institutions;
    """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[InstitutionQuery, NotUsed]): Future[Done] = {
    val sink = CassandraSink[InstitutionQuery](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.institutions").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
