package hmda.persistence.institutions

import akka.actor.ActorSystem
import akka.testkit.{ EventFilter, TestProbe }
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.processing.TestConfigOverride

class FilingPersistenceSpec extends ActorSpec {

  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        TestConfigOverride.config
      )
    )

  val filingsActor = createFilings("12345", system)

  val probe = TestProbe()

  "Filings" must {
    "be created and read back" in {
      val filings = DemoData.testFilings
      for (filing <- filings) {
        probe.send(filingsActor, CreateFiling(filing))
      }
      probe.send(filingsActor, GetState)
      probe.expectMsg(filings.reverse)
    }
    "be able to change their status" in {
      val filing = DemoData.testFilings.head
      probe.send(filingsActor, CreateFiling(filing))
      val modified = filing.copy(status = Cancelled)
      probe.send(filingsActor, UpdateFilingStatus(modified))
      probe.send(filingsActor, GetFilingByPeriod(filing.period))
      probe.expectMsg(filing.copy(status = Cancelled))
    }
  }

  "Error logging" must {

    "warn when creating a filing that already exists" in {
      // Setup: Persist a filing
      val f1 = Filing("2016", "12345", Completed)
      probe.send(filingsActor, CreateFiling(f1))

      //Test that warning is logged when identical filing is added
      val f2 = Filing("2016", "12345", Completed)
      val msg = s"Filing already exists. Could not create $f2"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(filingsActor, CreateFiling(f2))
      }
    }

    "warn when updating status for a nonexistent period" in {
      val f = Filing("2006", "12345", Cancelled)
      val msg = s"Period does not exist. Could not update $f"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(filingsActor, UpdateFilingStatus(f))
      }
    }
  }

}
