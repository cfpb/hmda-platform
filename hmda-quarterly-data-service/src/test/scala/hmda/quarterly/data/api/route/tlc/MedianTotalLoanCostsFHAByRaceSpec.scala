package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsFHAByRace


class MedianTotalLoanCostsFHAByRaceSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsFHAByRace.getRoute
  val routeSummary = MedianTotalLoanCostsFHAByRace.getSummary
  "median total loan costs fha by race route" should {
    "return an instance of GraphSeriesInfo" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}