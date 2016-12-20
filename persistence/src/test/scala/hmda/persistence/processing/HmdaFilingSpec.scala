package hmda.persistence.processing

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.parser.fi.lar.LarGenerators
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFiling._
import hmda.persistence.processing.HmdaFileValidator._

class HmdaFilingSpec extends ActorSpec with LarGenerators {

  val period = "2017"

  val lars10 = larListGen.sample.get

  val hmdaFiling = createHmdaFiling(system, period)

  val probe = TestProbe()

  "HMDA Filing" must {
    "Store 10 lars" in {
      for (lar <- lars10) {
        probe.send(hmdaFiling, AddLar(lar))
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
      probe.send(hmdaFiling, SaveLars(submissionId))
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(10))
    }
  }
}
