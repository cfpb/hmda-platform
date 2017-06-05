package hmda.query.repository.institutions

import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.{ Done, NotUsed }
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{ Sink, Source }
import com.datastax.driver.core.{ PreparedStatement, Row, SimpleStatement }
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.CassandraRepository

import scala.concurrent.Future

object InstitutionCassandraRepository extends CassandraRepository[InstitutionQuery] {

  override def createTable(): Unit = {
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

  override def dropTable(): Unit = {
    val query = s"""
      |DROP TABLE IF EXISTS $keyspace.institutions;
    """.stripMargin

    session.execute(query)
  }

  override def insertData(source: Source[InstitutionQuery, NotUsed]): Future[Done] = {
    val preparedStatement = session.prepare(s"INSERT INTO $keyspace.institutions VALUES (?)")
    val statementBinder = (institution: InstitutionQuery, statement: PreparedStatement) => statement.bind(institution)
    val sink = CassandraSink[InstitutionQuery](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

  override def readData(fetchSize: Int): Future[Seq[Row]] = {
    val statement = new SimpleStatement(s"SELECT * FROM $keyspace.institutions").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
