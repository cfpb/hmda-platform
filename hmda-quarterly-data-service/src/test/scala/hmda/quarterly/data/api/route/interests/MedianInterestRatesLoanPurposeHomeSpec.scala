package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesLoanPurposeHome


class MedianInterestRatesLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianInterestRatesLoanPurposeHome.getRoute
  val routeSummary = MedianInterestRatesLoanPurposeHome.getSummary
  "median interest rates loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median interest rates loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
