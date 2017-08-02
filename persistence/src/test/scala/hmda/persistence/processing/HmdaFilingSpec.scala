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

  val lars10 = get10SampleLars
  val lars20 = get10SampleLars ++ get10SampleLars

  val hmdaFiling = createHmdaFiling(system, period)

  val probe = TestProbe()

  "HMDA Filing" must {
    "Store 10 lars" in {
      for (lar <- lars10) {
        probe.send(hmdaFiling, LarValidated(lar, SubmissionId("12345", period, 0)))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(Map(s"12345-$period-0" -> 10)))
    }
    "Store additional 20 lars" in {
      for (lar <- lars20) {
        probe.send(hmdaFiling, LarValidated(lar, SubmissionId("9999", period, 1)))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(Map(s"12345-$period-0" -> 10, s"9999-$period-1" -> 20)))
    }
  }

  private def get10SampleLars = larListGen.sample.getOrElse(List[LoanApplicationRegister]())
}
