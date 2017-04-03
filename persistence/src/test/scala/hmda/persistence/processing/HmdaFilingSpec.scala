package hmda.persistence.processing

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFiling._
import hmda.persistence.processing.HmdaFileValidator._

class HmdaFilingSpec extends ActorSpec with LarGenerators {

  val period = "2017"

  val lars10 = larListGen.sample.getOrElse(List[LoanApplicationRegister]())

  val hmdaFiling = createHmdaFiling(system, period)

  val probe = TestProbe()

  "HMDA Filing" must {
    "Store 10 lars" in {
      for (lar <- lars10) {
        probe.send(hmdaFiling, LarValidated(lar, ""))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(10))
    }
    "read validated lars from HmdaFileValidator and save them" in {
      val submissionId = SubmissionId("12345", period, 1)
      val validator = createHmdaFileValidator(system, submissionId)
      for (lar <- lars10) {
        probe.send(validator, lar)
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(10))
    }
  }
}
