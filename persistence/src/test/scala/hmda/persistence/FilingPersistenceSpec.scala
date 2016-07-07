package hmda.persistence

import akka.testkit.TestProbe
import hmda.actor.test.ActorSpec
import hmda.model.fi.Cancelled
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.FilingPersistence.{ CreateFiling, GetFilingByPeriod, UpdateFilingStatus }
import hmda.persistence.demo.DemoData
import hmda.persistence.FilingPersistence._

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
}
