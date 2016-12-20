package hmda.query.view.filing

import akka.testkit.TestProbe
import hmda.persistence.model.ActorSpec
import hmda.query.view.filing.HmdaFilingView._

class HmdaFilingViewSpec extends ActorSpec {

  val period = "2017"

  val hmdaFilingView = createHmdaFilingView(system, period)

  val probe = TestProbe()
}
