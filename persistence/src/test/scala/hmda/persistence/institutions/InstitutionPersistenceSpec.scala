package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.{ FederalTaxId, RssdId, UndeterminedExternalId }
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec

class InstitutionPersistenceSpec extends ActorSpec {

  val institutionsActor = createInstitutions(system)

  val probe = TestProbe()

  "Institutions" must {
    "be created and read back" in {
      val institutions = DemoData.testInstitutions
      for (institution <- institutions) {
        probe.send(institutionsActor, CreateInstitution(institution))
        probe.expectMsg(Some(institution))
      }
      probe.send(institutionsActor, GetState)
      probe.expectMsg(institutions)
    }

    "get institution by id" in {
      val institution = DemoData.testInstitutions.head
      probe.send(institutionsActor, GetInstitution(institution.id))
      probe.expectMsg(Some(institution))
    }

    "be created, modified and read back" in {
      val institution = DemoData.testInstitutions.head
      val modifiedRespondent = institution.respondent.copy(name = "new name")
      val modified = institution.copy(respondent = modifiedRespondent)
      probe.send(institutionsActor, ModifyInstitution(modified))
      probe.expectMsg(Some(modified))
    }

    "Error logging" must {
      "warn when creating an institution that already exists" in {
        val i1 = DemoData.testInstitutions.head.copy(id = "123")
        probe.send(institutionsActor, CreateInstitution(i1))
        probe.expectMsg(Some(i1))

        val i2 = i1.copy()
        probe.send(institutionsActor, CreateInstitution(i2))
        probe.expectMsg(None)
      }

      "warn when updating nonexistent institution" in {
        val i = DemoData.testInstitutions.head.copy(id = "123456")
        probe.send(institutionsActor, ModifyInstitution(i))
        probe.expectMsg(None)
      }
    }
  }

}
