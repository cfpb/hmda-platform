package hmda.query.repository.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.Agency
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.CassandraRepositorySpec
import hmda.query.repository.filing.LarConverter._

class FilingCassandraRepositorySpec extends CassandraRepositorySpec[LoanApplicationRegisterQuery] with FilingCassandraRepository with LarGenerators {

  override def beforeAll(): Unit = {
    createKeyspace()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
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
      val readF = readData(100).runWith(Sink.seq)
      readF.map { lars =>
        lars.map(lar => lar.agencyCode mustBe Agency.CFPB.value)
        lars.size mustBe 100
      }
    }
  }

  override implicit def system: ActorSystem = ActorSystem()

  override implicit def materializer: ActorMaterializer = ActorMaterializer()
}
