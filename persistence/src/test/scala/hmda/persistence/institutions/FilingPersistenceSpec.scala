package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.model.ActorSpec

class FilingPersistenceSpec extends ActorSpec {

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
      val f1 = Filing("2016", "12345", Completed, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, CreateFiling(f1))

      //Test that warning is logged when identical filing is added
      val f2 = Filing("2016", "12345", Completed, 1483287071000L, 1514736671000L)
      val msg = s"Filing already exists. Could not create $f2"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(filingsActor, CreateFiling(f2))
      }
    }

    "warn when updating status for a nonexistent period" in {
      val f = Filing("2006", "12345", Cancelled, 1483287071000L, 1514736671000L)
      val msg = s"Period does not exist. Could not update $f"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(filingsActor, UpdateFilingStatus(f))
      }
    }
  }

}
