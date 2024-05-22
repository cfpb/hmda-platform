package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesCCByRaceLoanPurposeRefinance


class DenialRatesCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = DenialRatesCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = DenialRatesCCByRaceLoanPurposeRefinance.getSummary
  "denial rates cc by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "denial rates cc by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
