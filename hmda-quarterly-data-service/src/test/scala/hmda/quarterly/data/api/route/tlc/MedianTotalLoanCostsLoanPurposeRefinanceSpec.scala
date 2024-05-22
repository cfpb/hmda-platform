package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsLoanPurposeRefinance


class MedianTotalLoanCostsLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsLoanPurposeRefinance.getRoute
  val routeSummary = MedianTotalLoanCostsLoanPurposeRefinance.getSummary
  "median total loan costs loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median total loan costs loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
