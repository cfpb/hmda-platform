package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesFHAByRaceLoanPurposeRefinance


class MedianInterestRatesFHAByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianInterestRatesFHAByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianInterestRatesFHAByRaceLoanPurposeRefinance.getSummary
  "median interest rates fha by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median interest rates fha by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
