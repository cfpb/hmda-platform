package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVCCByRace


class MedianCLTVCCByRaceSpec extends WordSpec with Matchers {
  val route = MedianCLTVCCByRace.getRoute
  val routeSummary = MedianCLTVCCByRace.getSummary
  "median cltv cc by race route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median cltv cc by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
