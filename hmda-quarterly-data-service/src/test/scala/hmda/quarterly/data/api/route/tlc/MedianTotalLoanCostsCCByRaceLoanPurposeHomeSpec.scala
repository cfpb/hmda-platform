package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsCCByRaceLoanPurposeHome


class MedianTotalLoanCostsCCByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsCCByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianTotalLoanCostsCCByRaceLoanPurposeHome.getSummary
  "median total loan costs cc by race loan purpose home route" should {
    "return an instance of GraphSeriesInfo" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}