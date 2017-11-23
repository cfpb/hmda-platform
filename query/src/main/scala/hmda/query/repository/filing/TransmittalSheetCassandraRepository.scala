package hmda.query.repository.filing

import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.{ Flow, Source }
import com.typesafe.config.ConfigFactory
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.query.projections.ProjectionRuntime
import hmda.query.repository.CassandraRepository
import com.datastax.driver.core._

import scala.concurrent.Future

trait TransmittalSheetCassandraRepository extends CassandraRepository[TransmittalSheet] with ProjectionRuntime {

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
      "contact_email)" +
      " VALUES " +
      "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
  }

  val statementBinder = (ts: TransmittalSheet, statement: PreparedStatement) =>
    statement.bind(
      ts.respondentId + ts.agencyCode + ts.activityYear,
      new Integer(ts.agencyCode),
      new java.lang.Long(ts.timestamp),
      new Integer(ts.activityYear),
      ts.taxId,
      new Integer(ts.totalLines),
      ts.respondentId,
      ts.respondent.name,
      ts.respondent.address,
      ts.respondent.city,
      ts.respondent.state,
      ts.respondent.zipCode,
      ts.parent.name,
      ts.parent.address,
      ts.parent.city,
      ts.parent.state,
      ts.parent.zipCode,
      ts.contact.name,
      ts.contact.phone,
      ts.contact.fax,
      ts.contact.email
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
         |  contact_email varchar
         |);
       """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[TransmittalSheet, NotUsed]): Future[Done] = {
    val sink = CassandraSink[TransmittalSheet](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override protected def parseRows: Flow[Row, TransmittalSheet, NotUsed] = {
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

      TransmittalSheet(
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
    }
  }

}
