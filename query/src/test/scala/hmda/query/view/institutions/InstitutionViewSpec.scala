package hmda.query.view.institutions

import akka.actor.{ Actor, ActorRef }
import akka.testkit.TestProbe
import hmda.model.institution.{ Institution, InstitutionGenerators }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaQuery.EventWithSeqNr
import hmda.query.view.institutions.InstitutionView._
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef

class InstitutionViewSpec extends ActorSpec {

  val i1 = InstitutionGenerators.sampleInstitution
  val i2 = InstitutionGenerators.sampleInstitution
  val i3 = InstitutionGenerators.sampleInstitution
  val i4 = i3.copy(cra = true)

  val institutionQuery = createInstitutionView(system)

  implicit val ec = system.dispatcher

  val probe = TestProbe()

  override def beforeAll(): Unit = {
    super.beforeAll()
    institutionQuery ! EventWithSeqNr(1, InstitutionCreated(i1))
    institutionQuery ! EventWithSeqNr(2, InstitutionCreated(i2))
    institutionQuery ! EventWithSeqNr(3, InstitutionCreated(i3))
    institutionQuery ! EventWithSeqNr(4, InstitutionModified(i4))
  }

  "Institutions View" must {
    "return institution by id" in {
      probe.send(institutionQuery, GetInstitutionById(i1.id))
      probe.expectMsg(i1)
    }
    "return modified institution" in {
      probe.send(institutionQuery, GetInstitutionById(i3.id))
      probe.expectMsg(i4)
    }
    "return a set of institutions matching a list of ids" in {
      probe.send(institutionQuery, GetInstitutionsById(List(i1.id, i2.id)))
      probe.expectMsg(Set(i1, i2))
    }
    "return an empty set when requesting nonexistent institutions" in {
      probe.send(institutionQuery, GetInstitutionsById(List("a", "b")))
      probe.expectMsg(Set())
    }
    "return full list of institutions" in {
      probe.send(institutionQuery, GetState)
      probe.expectMsg(Set(i1, i2, i4))
    }
    "return reference to institution query projector" in {
      probe.send(institutionQuery, GetProjectionActorRef)
      probe.expectMsgType[ActorRef]
    }
  }

}
