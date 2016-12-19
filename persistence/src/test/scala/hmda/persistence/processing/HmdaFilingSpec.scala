package hmda.persistence.processing

import akka.testkit.TestProbe
import hmda.parser.fi.lar.LarGenerators
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFiling._

class HmdaFilingSpec extends ActorSpec with LarGenerators {

  val lars10 = larListGen.sample.get

  val hmdaFiling = createHmdaFiling(system, "2017")

  val probe = TestProbe()

  "HMDA Filing" must {
    "Store 10 lars" in {
      for (lar <- lars10) {
        probe.send(hmdaFiling, AddLar(lar))
        probe.expectMsg(LarAdded(lar))
      }
      probe.send(hmdaFiling, GetState)
      probe.expectMsg(HmdaFilingState(10))
    }
  }
}
