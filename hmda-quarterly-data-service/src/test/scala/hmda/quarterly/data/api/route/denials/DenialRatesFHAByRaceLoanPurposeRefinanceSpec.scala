package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.denials.DenialRatesFHAByRaceLoanPurposeRefinance


class DenialRatesFHAByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = DenialRatesFHAByRaceLoanPurposeRefinance.getRoute
  val routeSummary = DenialRatesFHAByRaceLoanPurposeRefinance.getSummary
  "denial rates fha by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "denial rates fha by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
