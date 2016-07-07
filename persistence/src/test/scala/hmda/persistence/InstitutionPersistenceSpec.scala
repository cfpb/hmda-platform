package hmda.persistence

import akka.testkit.TestProbe
import hmda.actor.test.ActorSpec
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.InstitutionPersistence.{ CreateInstitution, GetInstitutionById, ModifyInstitution }
import hmda.persistence.demo.DemoData
import hmda.persistence.InstitutionPersistence._

class InstitutionPersistenceSpec extends ActorSpec {

  val institutionsActor = createInstitutions(system)

  val probe = TestProbe()

  "Institutions" must {
    "be created and read back" in {
      val institutions = DemoData.institutions
      for (institution <- institutions) {
        probe.send(institutionsActor, CreateInstitution(institution))
      }
      probe.send(institutionsActor, GetState)
      probe.expectMsg(institutions)
    }
    "be created, modified and read back" in {
      val institution = DemoData.institutions.head
      probe.send(institutionsActor, CreateInstitution(institution))
      val modified = institution.copy(name = "new name")
      probe.send(institutionsActor, ModifyInstitution(modified))
      probe.send(institutionsActor, GetInstitutionById(modified.id))
      probe.expectMsg(modified)
    }
  }

}
