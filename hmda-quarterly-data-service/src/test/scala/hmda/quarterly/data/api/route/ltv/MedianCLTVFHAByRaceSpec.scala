package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVFHAByRace


class MedianCLTVFHAByRaceSpec extends WordSpec with Matchers {
  val route = MedianCLTVFHAByRace.getRoute
  val routeSummary = MedianCLTVFHAByRace.getSummary
  "median cltv fha by race route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median cltv fha by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
