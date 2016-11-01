package hmda.query.institutions

import akka.testkit.TestProbe
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.messages.events.institutions.InstitutionEvents.InstitutionCreated
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaQuery.EventWithSeqNr
import hmda.query.institutions.InstitutionQuery._

class InstitutionQuerySpec extends ActorSpec {

  val i1 = InstitutionGenerators.institutionGen.sample.get
  val i2 = InstitutionGenerators.institutionGen.sample.get

  val institutionQuery = createInstitutionQuery(system)

  val probe = TestProbe()

  override def beforeAll(): Unit = {
    super.beforeAll()
    institutionQuery ! EventWithSeqNr(1, InstitutionCreated(i1))
    institutionQuery ! EventWithSeqNr(2, InstitutionCreated(i2))
  }

  "Institutions Query" must {
    "return a set of institutions matching a list of ids" in {
      probe.send(institutionQuery, GetInstitutionsById(List(i1.id, i2.id)))
      probe.expectMsg(Set(i1, i2))
    }
    "return an empty set when requesting nonexistent institutions" in {
      probe.send(institutionQuery, GetInstitutionsById(List("a", "b")))
      probe.expectMsg(Set())
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

}
