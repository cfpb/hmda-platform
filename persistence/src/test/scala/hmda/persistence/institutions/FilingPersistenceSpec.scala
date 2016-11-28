package hmda.persistence.institutions

import akka.testkit.TestProbe
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
        probe.expectMsg(Some(filing))
      }
      probe.send(filingsActor, GetState)
      probe.expectMsg(filings.reverse)
    }
    "be able to change their status" in {
      val filing = DemoData.testFilings.head
      val modified = filing.copy(status = Cancelled)
      probe.send(filingsActor, UpdateFilingStatus(modified))
      probe.expectMsg(Some(modified))
      probe.send(filingsActor, GetFilingByPeriod(filing.period))
      probe.expectMsg(filing.copy(status = Cancelled))
    }

    "return None when persisting a filing that already exists" in {
      val f1 = Filing("2016", "12345", Completed, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, CreateFiling(f1))
      probe.expectMsg(Some(f1))

      val f2 = Filing("2016", "12345", Completed, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, CreateFiling(f2))
      probe.expectMsg(None)
    }

    "return None for nonexistent period" in {
      val f = Filing("2006", "12345", Cancelled, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, UpdateFilingStatus(f))
      probe.expectMsg(None)
    }
  }

}
