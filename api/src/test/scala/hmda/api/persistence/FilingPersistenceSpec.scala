package hmda.api.persistence

import akka.testkit.TestProbe
import hmda.api.demo.DemoData
import hmda.api.persistence.CommonMessages.GetState
import hmda.api.processing.ActorSpec
import hmda.api.persistence.FilingPersistence._
import hmda.model.fi.Cancelled

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
      probe.send(filingsActor, GetFilingById(filing.period))
      probe.expectMsg(filing.copy(status = Cancelled))
    }
  }
}
