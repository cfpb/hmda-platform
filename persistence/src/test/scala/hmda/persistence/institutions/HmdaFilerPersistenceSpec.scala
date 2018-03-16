package hmda.persistence.institutions

import hmda.persistence.model.{ ActorSpec, AsyncActorSpec }
import akka.pattern.ask
import HmdaFilerPersistence._
import akka.actor.ActorRef
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import hmda.persistence.HmdaSupervisor._
import hmda.validation.stats.ValidationStats._

import scala.concurrent.Await
import scala.concurrent.duration._

class HmdaFilerPersistenceSpec extends ActorSpec {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor.timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = system.dispatcher

  val validationStats = createValidationStats(system)
  val supervisor = createSupervisor(system, validationStats)
  val hmdaFilersF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name))
    .mapTo[ActorRef]

  val hmdaFilers = Await.result(hmdaFilersF, duration.seconds)

  val probe = TestProbe()

  val hmdaFiler1 = HmdaFiler("12345", "respId", "2017", "Bank")
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
