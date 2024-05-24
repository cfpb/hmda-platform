package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.dti.MedianDTIByTypeLoanPurposeHome


class MedianDTIByTypeLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianDTIByTypeLoanPurposeHome.getRoute
  val routeSummary = MedianDTIByTypeLoanPurposeHome.getSummary
  "median dti by type loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median dti by type loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
