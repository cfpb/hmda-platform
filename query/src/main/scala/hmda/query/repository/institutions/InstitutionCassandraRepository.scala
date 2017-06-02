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
          |id varchar PRIMARY KEY,
          |agency int,
          |filingPeriod int,
          |activityYear int,
          |respondentId varchar,
          |institutionType varchar,
          |cra boolean,
          |emailDomain1 varchar,
          |emailDomain2 varchar,
          |emailDomain3 varchar,
          |respondentName varchar,
          |respondentState varchar,
          |respondentCity varchar,
          |respondentFipsStateNumber varchar,
          |hmdaFilerFlag boolean,
          |parentRespondentId varchar,
          |parentIdRssd int,
          |parentName varchar,
          |assets int,
          |otherLenderCode int,
          |topHolderIdRssd int,
          |topHolderName varchar,
          |topHolderCity varchar,
          |topHolderState varchar,
          |topHolderCountry varchar
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
    val statement = new SimpleStatement(s"SELECT FROM $keyspace.institutions").setFetchSize(fetchSize)
    CassandraSource(statement).runWith(Sink.seq).mapTo[Seq[Row]]
  }

}
