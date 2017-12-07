package hmda.query.repository.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.Agency
import hmda.query.repository.CassandraRepositorySpec

import scala.concurrent.Await
import scala.concurrent.duration._

class LoanApplicationRegisterCassandraRepositorySpec extends CassandraRepositorySpec[LoanApplicationRegister] with LoanApplicationRegisterCassandraRepository with LarGenerators {

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

      val lar = larGen.sample.getOrElse(LoanApplicationRegister())

      val lars = lar100ListGen.sample.getOrElse(List(lar))
        .map(x => x.copy(agencyCode = 9))

      val source = Source
        .fromIterator(() => lars.toIterator)

      val readF = for {
        _ <- insertData(source)
        data <- readData(100).runWith(Sink.seq)
      } yield data

      val xs = Await.result(readF, 20.seconds)
      xs.map(lar => lar.agencyCode mustBe Agency.CFPB.value)
      xs.size mustBe lars.size
    }
  }

  override implicit def system: ActorSystem = ActorSystem()

  override implicit def materializer: ActorMaterializer = ActorMaterializer()
}
