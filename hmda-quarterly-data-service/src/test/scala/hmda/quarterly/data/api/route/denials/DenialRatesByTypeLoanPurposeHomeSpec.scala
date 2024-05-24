package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.denials.DenialRatesByTypeLoanPurposeHome


class DenialRatesByTypeLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = DenialRatesByTypeLoanPurposeHome.getRoute
  val routeSummary = DenialRatesByTypeLoanPurposeHome.getSummary
  "denial rates by type loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
}
