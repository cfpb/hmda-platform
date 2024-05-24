package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVFHAByRaceLoanPurposeRefinance


class MedianCLTVFHAByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianCLTVFHAByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianCLTVFHAByRaceLoanPurposeRefinance.getSummary
  "median cltvfha by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median cltvfha by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
