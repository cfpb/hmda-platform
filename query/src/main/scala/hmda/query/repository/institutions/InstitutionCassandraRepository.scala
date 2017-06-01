package hmda.query.repository.institutions

import com.datastax.driver.core.ResultSet
import hmda.query.repository.CassandraRepository


object InstitutionCassandraRepository extends CassandraRepository {

  override def createTable(): ResultSet = {
    val query =
      """
        |CREATE TABLE IF NOT EXISTS institutions(
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

  override def deleteTable(): Unit = {
    val query = """
      |DROP TABLE institutions;
    """.stripMargin

    session.execute(query)
  }


}
