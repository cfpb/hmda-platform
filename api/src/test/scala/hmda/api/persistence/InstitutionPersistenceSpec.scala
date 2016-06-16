package hmda.api.persistence

import java.io.File

import akka.testkit.TestProbe
import hmda.api.demo.DemoData
import hmda.api.persistence.CommonMessages.GetState
import hmda.api.processing.ActorSpec
import hmda.api.persistence.InstitutionPersistence._

class InstitutionPersistenceSpec extends ActorSpec {

  val file = new File("api/src/main/resources/institutions.json")

  val institutionsActor = createInstitutionsFiling(system)

  val probe = TestProbe()

  "Institution Filings" must {
    "be created and read back" in {
      val institutions = DemoData(file).institutions
      for (institution <- institutions) {
        probe.send(institutionsActor, CreateInstitution(institution))
      }
      probe.send(institutionsActor, GetState)
      probe.expectMsg(institutions)
    }
    "be created, modified and read back" in {
      val institution = DemoData(file).institutions.head
      probe.send(institutionsActor, CreateInstitution(institution))
      val modified = institution.copy(name = "new name")
      probe.send(institutionsActor, ModifyInstitution(modified))
      probe.send(institutionsActor, GetInstitutionById(modified.id))
      probe.expectMsg(modified)
    }
  }

}
