package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import monix.execution.CancelableFuture
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesInfo

import hmda.quarterly.data.api.route.rates.dti.MedianDTICCByRaceLoanPurposeRefinance


class MedianDTICCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianDTICCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianDTICCByRaceLoanPurposeRefinance.getSummary
  "median dti cc by race loan purpose refinance route" should {
    "return an instance of GraphSeriesInfo" in {
      assert(routeSummary.isInstanceOf[CancelableFuture[GraphSeriesInfo]])
    }
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}