package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.{ FederalTaxId, RssdId }
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution.{ Active, ExternalId, Institution }
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
        probe.expectMsg(institution)
      }
      probe.send(institutionsActor, GetState)
      probe.expectMsg(institutions)
    }
    "be created, modified and read back" in {
      val institution = DemoData.testInstitutions.head
      val modified = institution.copy(name = "new name")
      probe.send(institutionsActor, ModifyInstitution(modified))
      probe.expectMsg(Some(modified))
    }

    "Error logging" must {

      "warn when creating an institution that already exists" in {
        // Setup: Persist an institution
        val i1 = Institution("12345", "Test Bank 1", Set(ExternalId("99-1234567", FederalTaxId), ExternalId("123456", RssdId)), CFPB, Bank, hasParent = true, status = Active)
        probe.send(institutionsActor, CreateInstitution(i1))
        probe.expectMsg(i1)

        // Attempt to add identical institution; test that warning is logged
        val i2 = i1.copy()
        val msg = s"Institution already exists. Could not create $i2"
        EventFilter.warning(message = msg, occurrences = 1) intercept {
          probe.send(institutionsActor, CreateInstitution(i2))
        }
      }

      "warn when updating nonexistent institution" in {
        val i = Institution("123456", "Bogus bank", Set(ExternalId("99-7654321", FederalTaxId), ExternalId("654321", RssdId)), CFPB, Bank, hasParent = true, status = Active)
        val msg = s"Institution does not exist. Could not update $i"
        EventFilter.warning(message = msg, occurrences = 1) intercept {
          probe.send(institutionsActor, ModifyInstitution(i))
          probe.expectMsg(None)
        }
      }
    }
  }

}
