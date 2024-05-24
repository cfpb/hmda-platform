package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.dti.MedianDTIByType


class MedianDTIByTypeSpec extends WordSpec with Matchers {
  val route = MedianDTIByType.getRoute
  val routeSummary = MedianDTIByType.getSummary
  "median dti by type route" should {
    "return the correct summary route" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
  }
  "median dti by type route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
