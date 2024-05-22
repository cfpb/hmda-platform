package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsFHAByRace


class MedianTotalLoanCostsFHAByRaceSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsFHAByRace.getRoute
  val routeSummary = MedianTotalLoanCostsFHAByRace.getSummary
  "median total loan costs fha by race route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median total loan costs fha by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
