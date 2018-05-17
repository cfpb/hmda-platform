package hmda.persistence.institutions

import akka.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import hmda.persistence.institutions.InstitutionPersistence.{
  CreateInstitution,
  InstitutionCreated,
  InstitutionEvent
}
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import hmda.model.institutions.InstitutionGenerators._
import hmda.persistence.util.CassandraUtil

class InstitutionAsyncPersistenceSpec
    extends WordSpec
    with ActorTestKit
    with BeforeAndAfterAll {

  override def beforeAll(): Unit = CassandraUtil.startEmbeddedCassandra()

  override def afterAll(): Unit = shutdownTestKit()

  val probe = TestProbe[InstitutionEvent]
  //val institutionPersistence = spawn(
  //  InstitutionPersistence.behavior("ABC12345"))

  val sampleInstitution = institutionGen.sample.get

  "An institution" must {
    "Be created" in {
      //      institutionPersistence ! CreateInstitution(sampleInstitution, probe.ref)
      //      probe.expectMessage(InstitutionCreated(sampleInstitution))
    }
  }

}
