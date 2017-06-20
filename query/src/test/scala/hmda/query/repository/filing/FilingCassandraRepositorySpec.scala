package hmda.query.repository.filing

import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.Agency
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.CassandraRepositorySpec
import hmda.query.repository.filing.LarConverter._

class FilingCassandraRepositorySpec extends CassandraRepositorySpec[LoanApplicationRegisterQuery] with FilingCassandraRepository with LarGenerators {

  override def beforeAll(): Unit = {
    createKeyspace()
  }

  "LAR in Cassandra" must {
    "Drop the table if it exists, create it again and populate it with some data that can be read back" in {
      dropTable()
      createTable()

      val lars = lar100ListGen.sample.get.map(x => x.copy(agencyCode = 9))
      val source = Source
        .fromIterator(() => lars.toIterator)
        .map(lar => toLoanApplicationRegisterQuery(lar))
      insertData(source)
      val read = readData(100)
      read.map { r =>
        r.map(x => x.getInt("agency") mustBe Agency.CFPB.value)
        r.seq.size mustBe 100
      }
    }
  }

}
