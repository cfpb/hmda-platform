package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVFHAByRaceLoanPurposeHome


class MedianCLTVFHAByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianCLTVFHAByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianCLTVFHAByRaceLoanPurposeHome.getSummary
  "median cltvfha by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median cltvfha by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
