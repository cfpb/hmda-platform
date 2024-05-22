package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTIFHAByRaceLoanPurposeRefinance


class MedianDTIFHAByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianDTIFHAByRaceLoanPurposeRefinance.getRoute
  val routeSummary = MedianDTIFHAByRaceLoanPurposeRefinance.getSummary
  "median dtifha by race loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median dtifha by race loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
