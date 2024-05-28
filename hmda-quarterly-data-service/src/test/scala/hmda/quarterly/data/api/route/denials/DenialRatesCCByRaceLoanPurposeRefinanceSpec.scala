package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.denials.DenialRatesCCByRaceLoanPurposeRefinance


class DenialRatesCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = DenialRatesCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = DenialRatesCCByRaceLoanPurposeRefinance.getSummary
  "denial rates cc by race loan purpose refinance route" should {
    "return an instance of GraphSeriesInfo" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}