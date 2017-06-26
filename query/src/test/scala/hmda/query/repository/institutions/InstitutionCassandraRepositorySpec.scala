package hmda.query.repository.institutions

import akka.stream.scaladsl.Source
import hmda.model.institution.{ Agency, InstitutionGenerators }
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.CassandraRepositorySpec
import hmda.query.repository.institutions.InstitutionConverter._

class InstitutionCassandraRepositorySpec extends CassandraRepositorySpec[InstitutionQuery] with InstitutionCassandraRepository {

  override def beforeAll(): Unit = {
    createKeyspace()
  }

  "Institutions in Cassandra" must {
    "Drop the table if it exists, create it again and populate it with some data that can be read back" in {
      dropTable()
      createTable()

      val institutions = List(
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB))
      )

      val source = Source.fromIterator(() => institutions.toIterator)
      insertData(source)
      val read = readData(20)
      read.map { r =>
        r.map(x => x.getInt("agency") mustBe Agency.CFPB.value)
        r.seq.size mustBe 3
      }
    }
  }

}
