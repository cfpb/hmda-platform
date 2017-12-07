package hmda.query.repository.institutions

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.institution.{ Agency, InstitutionGenerators }
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.CassandraRepositorySpec
import hmda.query.repository.institutions.InstitutionConverter._
import scala.concurrent.duration._
import scala.concurrent.Await

class InstitutionCassandraRepositorySpec extends CassandraRepositorySpec[InstitutionQuery] with InstitutionCassandraRepository {

  override def beforeAll(): Unit = {
    createKeyspace()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
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

      val readF = for {
        _ <- insertData(source)
        data <- readData(20).runWith(Sink.seq)
      } yield data

      val xs = Await.result(readF, 10.seconds)
      xs.map(i => i.agency mustBe Agency.CFPB.value)
      xs.size mustBe 3
    }
  }

  override implicit def system: ActorSystem = ActorSystem()

  override implicit def materializer: ActorMaterializer = ActorMaterializer()
}
