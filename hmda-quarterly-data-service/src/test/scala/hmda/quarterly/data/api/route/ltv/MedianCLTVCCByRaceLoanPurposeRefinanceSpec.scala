package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVCCByRaceLoanPurposeRefinance


class MedianCLTVCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianCLTVCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianCLTVCCByRaceLoanPurposeRefinance.getSummary
  "median cltv cc by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median cltv cc by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
