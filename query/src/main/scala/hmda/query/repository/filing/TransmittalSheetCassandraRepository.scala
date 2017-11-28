package hmda.query.repository.filing

import java.time.LocalDateTime

import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.{ Flow, Source }
import com.typesafe.config.ConfigFactory
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository
import com.datastax.driver.core._
import hmda.query.model.filing.TransmittalSheetWithTimestamp

import scala.concurrent.Future

trait TransmittalSheetCassandraRepository extends CassandraRepository[TransmittalSheetWithTimestamp] with ProjectionRuntime {

  val config = ConfigFactory.load()
  val table = config.getString("hmda.table.ts")

  def preparedStatement(implicit session: Session): PreparedStatement = {
    session.prepare(s"INSERT INTO $keyspace.$table" +
      "(id," +
      "agency_code," +
      "timestamp," +
      "activity_year," +
      "tax_id," +
      "total_lines," +
      "respondent_id," +
      "respondent_name," +
      "respondent_address," +
      "respondent_city," +
      "respondent_state," +
      "respondent_zipcode," +
      "parent_name," +
      "parent_address," +
      "parent_city," +
      "parent_state," +
      "parent_zipcode," +
      "contact_name," +
      "contact_phone," +
      "contact_fax," +
      "contact_email," +
      "submission_timestamp)" +
      " VALUES " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
  }

  val statementBinder = (t: TransmittalSheetWithTimestamp, statement: PreparedStatement) =>
    statement.bind(
      t.ts.respondentId + t.ts.agencyCode + t.ts.activityYear,
      new Integer(t.ts.agencyCode),
      new java.lang.Long(t.ts.timestamp),
      new Integer(t.ts.activityYear),
      t.ts.taxId,
      new Integer(t.ts.totalLines),
      t.ts.respondentId,
      t.ts.respondent.name,
      t.ts.respondent.address,
      t.ts.respondent.city,
      t.ts.respondent.state,
      t.ts.respondent.zipCode,
      t.ts.parent.name,
      t.ts.parent.address,
      t.ts.parent.city,
      t.ts.parent.state,
      t.ts.parent.zipCode,
      t.ts.contact.name,
      t.ts.contact.phone,
      t.ts.contact.fax,
      t.ts.contact.email,
      LocalDateTime.now().toString
    )

  override def createTable(): ResultSet = {
    val query =
      s"""
         |CREATE TABLE IF NOT EXISTS $keyspace.$table(
         |  id varchar PRIMARY KEY,
         |  agency_code int,
         |  timestamp bigint,
         |  activity_year int,
         |  tax_id varchar,
         |  total_lines int,
         |  respondent_id varchar,
         |  respondent_name varchar,
         |  respondent_address varchar,
         |  respondent_city varchar,
         |  respondent_state varchar,
         |  respondent_zipcode varchar,
         |  parent_name varchar,
         |  parent_address varchar,
         |  parent_city varchar,
         |  parent_state varchar,
         |  parent_zipcode varchar,
         |  contact_name varchar,
         |  contact_phone varchar,
         |  contact_fax varchar,
         |  contact_email varchar,
         |  submission_timestamp varchar
         |);
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[TransmittalSheetWithTimestamp, NotUsed]): Future[Done] = {
    val sink = CassandraSink[TransmittalSheetWithTimestamp](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override protected def parseRows: Flow[Row, TransmittalSheetWithTimestamp, NotUsed] = {
    Flow[Row].map { row =>
      val agencyCode = row.getInt("agency_code")
      val timestamp = row.getLong("timestamp")
      val activity_year = row.getInt("activity_year")
      val taxId = row.getString("tax_id")
      val totalLines = row.getInt("total_lines")
      val respondentId = row.getString("respondent_id")
      val respondentName = row.getString("respondent_name")
      val respondentAddress = row.getString("respondent_address")
      val respondentCity = row.getString("respondent_city")
      val respondentState = row.getString("respondent_state")
      val respondentZipCode = row.getString("respondent_zipcode")
      val parentName = row.getString("parent_name")
      val parentAddress = row.getString("parent_address")
      val parentCity = row.getString("parent_city")
      val parentState = row.getString("parent_state")
      val parentZipCode = row.getString("parent_zipcode")
      val contactName = row.getString("contact_name")
      val contactPhone = row.getString("contact_phone")
      val contactFax = row.getString("contact_fax")
      val contactEmail = row.getString("contact_email")
      val submission_timestamp = row.getString("submission_timestamp")

      val respondent = Respondent(
        respondentId,
        respondentName,
        respondentAddress,
        respondentCity,
        respondentState,
        respondentZipCode
      )

      val parent = Parent(
        parentName,
        parentAddress,
        parentCity,
        parentState,
        parentZipCode
      )

      val contact = Contact(
        contactName,
        contactPhone,
        contactFax,
        contactEmail
      )

      val ts = TransmittalSheet(
        1,
        agencyCode,
        timestamp,
        activity_year,
        taxId,
        totalLines,
        respondent,
        parent,
        contact
      )
      TransmittalSheetWithTimestamp(ts, submission_timestamp)
    }
  }

}
