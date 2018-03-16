package hmda.persistence.institutions

import hmda.persistence.model.{ ActorSpec, AsyncActorSpec }
import akka.pattern.ask
import HmdaFilerPersistence._
import akka.actor.ActorRef
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, HmdaFiler, Institution, Respondent }
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.commands.institutions.InstitutionCommands.CreateInstitution
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.validation.stats.ValidationStats._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration._

class HmdaFilerPersistenceSpec extends ActorSpec with BeforeAndAfterAll {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor.timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = system.dispatcher

  val validationStats = createValidationStats(system)
  val supervisor = createSupervisor(system, validationStats)
  val hmdaFilersF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name))
    .mapTo[ActorRef]

  val institutionsF = (supervisor ? FindActorByName(InstitutionPersistence.name))
    .mapTo[ActorRef]

  val hmdaFilers = Await.result(hmdaFilersF, duration.seconds)
  val institutions = Await.result(institutionsF, duration.seconds)

  val i = Institution.empty.copy(id = "abc", activityYear = 2017, respondent = Respondent(ExternalId("respId 1", RssdId), name = "Bank 0"))

  val probe = TestProbe()

  override def beforeAll(): Unit = {
    super.beforeAll()
    institutions ! CreateInstitution(i)
  }

  val hmdaFiler1 = HmdaFiler("12345", "respId 2", "2017", "Bank 1")
  val hmdaFiler2 = HmdaFiler("abc", "respId 1", "2017", "Bank 0")

  "Hmda Filers" must {
    "return empty when there is nothing saved" in {
      probe.send(hmdaFilers, FindHmdaFiler(hmdaFiler1.institutionId))
      probe.expectMsg(None)
    }
    "return nothing when deleting empty journal" in {
      probe.send(hmdaFilers, DeleteHmdaFiler(hmdaFiler1))
      probe.expectMsg(None)
    }
    "be created and read back" in {
      probe.send(hmdaFilers, CreateHmdaFiler(hmdaFiler1))
      probe.expectMsg(HmdaFilerCreated(hmdaFiler1))
      probe.send(hmdaFilers, FindHmdaFiler(hmdaFiler1.institutionId))
      probe.expectMsg(Some(hmdaFiler1))
    }
    "delete" in {
      probe.send(hmdaFilers, DeleteHmdaFiler(hmdaFiler1))
      probe.expectMsg(Some(HmdaFilerDeleted(hmdaFiler1)))
      probe.send(hmdaFilers, FindHmdaFiler(hmdaFiler1.institutionId))
      probe.expectMsg(None)
    }

    "Create filer from submission signed event" in {
      val submissionId = SubmissionId("abc", "2017", 1)
      probe.send(hmdaFilers, SubmissionSignedPubSub(submissionId))
      probe.expectMsg(HmdaFilerCreated(hmdaFiler2))
      probe.send(hmdaFilers, GetState)
      probe.expectMsg(Set(hmdaFiler2))

    }
  }

}
