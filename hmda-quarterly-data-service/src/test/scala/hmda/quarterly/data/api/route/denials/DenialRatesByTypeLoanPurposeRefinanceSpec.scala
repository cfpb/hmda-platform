package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesByTypeLoanPurposeRefinance


class DenialRatesByTypeLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = DenialRatesByTypeLoanPurposeRefinance.getRoute
  val routeSummary = DenialRatesByTypeLoanPurposeRefinance.getSummary
  "denial rates by type loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "denial rates by type loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
