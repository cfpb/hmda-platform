package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCosts


class MedianTotalLoanCostsSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCosts.getRoute
  val routeSummary = MedianTotalLoanCosts.getSummary
  "median total loan costs route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median total loan costs route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
