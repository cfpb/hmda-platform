package hmda.persistence.institutions

import akka.testkit.TestProbe
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.model.ActorSpec

class FilingPersistenceSpec extends ActorSpec {

  val filingsActor = createFilings("12345", system)

  val sample1 = Filing("2016", "12345", Completed, filingRequired = true, 1483287071000L, 1514736671000L)
  val sample2 = Filing("2017", "12345", NotStarted, filingRequired = true, 0L, 0L)

  val probe = TestProbe()

  "CreateFiling" must {
    "create a filing" in {
      val filings = Seq(sample1, sample2)
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
      probe.expectMsg(modified)
    }

    "return None when persisting a filing period that already exists" in {
      val f1 = Filing("2018", "12345", Completed, true, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, CreateFiling(f1))
      probe.expectMsg(Some(f1))

      val f2 = Filing("2016", "12345", InProgress, true, 1483287071000L, 0L)
      probe.send(filingsActor, CreateFiling(f2))
      probe.expectMsg(None)
    }

    "return None for nonexistent period" in {
      val f = Filing("2006", "12345", Cancelled, false, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, UpdateFilingStatus(f))
      probe.expectMsg(None)
    }
  }

}
