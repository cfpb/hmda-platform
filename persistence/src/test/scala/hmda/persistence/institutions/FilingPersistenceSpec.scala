package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.actor.test.ActorSpec
import hmda.model.fi.{ Cancelled, Completed, Filing }
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.FilingPersistence.{ CreateFiling, GetFilingByPeriod, UpdateFilingStatus, _ }

class FilingPersistenceSpec extends ActorSpec {

  val filingsActor = createFilings("12345", system)

  val probe = TestProbe()

  "Filings" must {
    "be created and read back" in {
      val filings = DemoData.filings
      for (filing <- filings) {
        probe.send(filingsActor, CreateFiling(filing))
      }
      probe.send(filingsActor, GetState)
      probe.expectMsg(filings.reverse)
    }
    "be able to change their status" in {
      val filing = DemoData.filings.head
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
  }
}
