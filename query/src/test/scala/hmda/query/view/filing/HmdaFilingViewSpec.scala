package hmda.query.view.filing

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaQuery.EventWithSeqNr
import hmda.query.view.filing.HmdaFilingView._

class HmdaFilingViewSpec extends ActorSpec with LarGenerators {

  val l1 = sampleLar
  val l2 = sampleLar
  val l3 = sampleLar

  val period = "2017"

  val hmdaFilingView = createHmdaFilingView(system, period)

  val probe = TestProbe()

  override def beforeAll(): Unit = {
    super.beforeAll()
    hmdaFilingView ! EventWithSeqNr(1, LarValidated(l1, SubmissionId()))
    hmdaFilingView ! EventWithSeqNr(2, LarValidated(l2, SubmissionId()))
    hmdaFilingView ! EventWithSeqNr(3, LarValidated(l3, SubmissionId()))
  }

  "HMDA Filing View" must {
    val state = FilingViewState(3, 3)
    "return filing view state" in {
      probe.send(hmdaFilingView, GetState)
      probe.expectMsg(state)
    }
  }

}
