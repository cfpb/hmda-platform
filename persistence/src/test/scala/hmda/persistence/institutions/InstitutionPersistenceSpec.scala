package hmda.persistence.institutions

import akka.testkit.TestProbe
import hmda.model.institution.Institution
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.commands.institutions.InstitutionCommands._
import hmda.persistence.model.ActorSpec
import hmda.model.institution.InstitutionGenerators._

class InstitutionPersistenceSpec extends ActorSpec {

  val institutionsActor = createInstitutions(system)

  val headInst = DemoData.testInstitutions.head

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
      probe.send(institutionsActor, GetInstitutionById(headInst.id))
      probe.expectMsg(Some(headInst))
    }

    "delete institution" in {
      val institution = institutionGen.sample.getOrElse(Institution.empty)
      probe.send(institutionsActor, CreateInstitution(institution))
      probe.expectMsg(Some(institution))

      probe.send(institutionsActor, GetInstitutionById(institution.id))
      probe.expectMsg(Some(institution))

      probe.send(institutionsActor, DeleteInstitution(institution))
      probe.expectMsg(Some(institution))

      probe.send(institutionsActor, GetInstitutionById(institution.id))
      probe.expectMsg(None)

      probe.send(institutionsActor, DeleteInstitution(institutionGen.sample.getOrElse(Institution.empty)))
      probe.expectMsg(None)

    }

    "get institutions by id" in {
      val ids = DemoData.testInstitutions.map(i => i.id).toList
      probe.send(institutionsActor, GetInstitutionsById(ids))
      probe.expectMsg(DemoData.testInstitutions)
    }

    "return an empty set when requesting non-existant ids" in {
      probe.send(institutionsActor, GetInstitutionsById(List("a", "b")))
      probe.expectMsg(Set())
    }

    "get institution by respondent id" in {
      val respondentId = headInst.respondent.externalId.value
      probe.send(institutionsActor, GetInstitutionByRespondentId(respondentId))
      probe.expectMsg(headInst)
    }

    "return an empty institution for a non-existent respondent id" in {
      probe.send(institutionsActor, GetInstitutionByRespondentId("fakeId"))
      probe.expectMsg(Institution.empty)
    }

    "find institution by email domain" in {
      probe.send(institutionsActor, FindInstitutionByDomain("bank0.com"))
      probe.expectMsg(Set(headInst))
    }

    "return an empty set when requesting a domain that doesn't exist" in {
      probe.send(institutionsActor, FindInstitutionByDomain("fake.com"))
      probe.expectMsg(Set())
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
