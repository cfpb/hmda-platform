package hmda.persistence.institutions

import akka.testkit.TestProbe
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.model.ActorSpec
import hmda.persistence.messages.commands.filing.FilingCommands._

class FilingPersistenceSpec extends ActorSpec {

  val filingsActor = createFilings("12345", system)

  val sample1 = Filing("2016", "12345", Cancelled, filingRequired = true, 1483287071000L, 1514736671000L)
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
    "return Some when creating filing for period with no filings" in {
      val f1 = Filing("2018", "12345", Completed, true, 1483287071000L, 1514736671000L)
      probe.send(filingsActor, CreateFiling(f1))
      probe.expectMsg(Some(f1))
    }
    "return None when persisting a filing period that already has a filing" in {
      val f2 = Filing("2016", "12345", InProgress, true, 1483287071000L, 0L)
      probe.send(filingsActor, CreateFiling(f2))
      probe.expectMsg(None)
    }
  }

  "UpdateFilingStatus" must {
    "update status of filing for given period" in {
      val expected = sample1.copy(status = NotStarted)
      probe.send(filingsActor, UpdateFilingStatus("2016", NotStarted))
      probe.expectMsg(Some(expected))
      probe.send(filingsActor, GetFilingByPeriod("2016"))
      probe.expectMsg(expected)
    }
    "return None for nonexistent filing period" in {
      probe.send(filingsActor, UpdateFilingStatus("2006", InProgress))
      probe.expectMsg(None)
    }
    "update start and end timestamp, if necessary" in {
      probe.send(filingsActor, UpdateFilingStatus("2017", InProgress))
      val inProg = probe.expectMsgType[Option[Filing]].get
      inProg.status mustBe InProgress
      val startTime = inProg.start
      startTime must be > 0L
      inProg.end mustBe 0L

      probe.send(filingsActor, UpdateFilingStatus("2017", Completed))
      val comp = probe.expectMsgType[Option[Filing]].get
      comp.status mustBe Completed
      comp.start mustBe startTime
      comp.end must be > 0L
    }
    "not update if filing status is already 'Completed'" in {
      probe.send(filingsActor, UpdateFilingStatus("2017", InProgress))
      probe.expectMsg(None)
    }
  }

}
