package hmda.persistence.institutions

import hmda.persistence.model.ActorSpec
import HmdaFilerPersistence._
import akka.actor.ActorRef
import akka.testkit.TestProbe
import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }

class HmdaFilerPersistenceSpec extends ActorSpec {

  val hmdaFilers: ActorRef = createHmdaFilers(system)

  val probe = TestProbe()

  val hmdaFiler1 = HmdaFiler("12345", "respId", "99-999999", "2017", "Bank")
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
  }

}
