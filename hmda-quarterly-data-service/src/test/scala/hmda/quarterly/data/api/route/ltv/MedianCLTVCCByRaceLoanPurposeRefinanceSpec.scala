package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVCCByRaceLoanPurposeRefinance


class MedianCLTVCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianCLTVCCByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianCLTVCCByRaceLoanPurposeRefinance.getSummary
  "median cltvcc by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median cltvcc by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
