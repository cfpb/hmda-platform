package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsCCByRaceLoanPurposeRefinance


class MedianTotalLoanCostsCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianTotalLoanCostsCCByRaceLoanPurposeRefinance.getSummary
  "median total loan costs cc by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median total loan costs cc by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
