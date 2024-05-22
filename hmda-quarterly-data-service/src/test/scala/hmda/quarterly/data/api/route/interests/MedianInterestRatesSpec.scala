package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.interests.MedianInterestRates


class MedianInterestRatesSpec extends WordSpec with Matchers {
  val route = MedianInterestRates.getRoute
  val routeSummary = MedianInterestRates.getSummary
  "median interest rates route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median interest rates route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
