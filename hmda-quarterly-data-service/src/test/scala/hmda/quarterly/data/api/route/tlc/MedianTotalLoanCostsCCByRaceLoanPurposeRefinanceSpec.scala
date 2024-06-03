package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsCCByRaceLoanPurposeRefinance


class MedianTotalLoanCostsCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianTotalLoanCostsCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianTotalLoanCostsCCByRaceLoanPurposeRefinance.getSummary
  "median total loan costs cc by race loan purpose refinance route" should {
    "return an instance of GraphSeriesInfo" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}