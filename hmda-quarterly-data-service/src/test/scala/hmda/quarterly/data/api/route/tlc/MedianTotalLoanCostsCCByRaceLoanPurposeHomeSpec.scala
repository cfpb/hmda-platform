package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsCCByRaceLoanPurposeHome


class MedianTotalLoanCostsCCByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsCCByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianTotalLoanCostsCCByRaceLoanPurposeHome.getSummary
  "median total loan costs cc by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median total loan costs cc by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
