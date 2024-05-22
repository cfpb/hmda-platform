package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesByTypeLoanPurposeHome


class DenialRatesByTypeLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = DenialRatesByTypeLoanPurposeHome.getRoute
  val routeSummary = DenialRatesByTypeLoanPurposeHome.getSummary
  "denial rates by type loan purpose home route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "denial rates by type loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
