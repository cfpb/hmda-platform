package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesFHAByRaceLoanPurposeHome


class MedianInterestRatesFHAByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianInterestRatesFHAByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianInterestRatesFHAByRaceLoanPurposeHome.getSummary
  "median interest rates fha by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median interest rates fha by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
