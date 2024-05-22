package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesByType


class DenialRatesByTypeSpec extends WordSpec with Matchers {
  val route = DenialRatesByType.getRoute
  val routeSummary = DenialRatesByType.getSummary
  "denial rates by type route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "denial rates by type route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
