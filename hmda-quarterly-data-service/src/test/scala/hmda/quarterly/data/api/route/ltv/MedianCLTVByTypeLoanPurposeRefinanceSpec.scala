package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVByTypeLoanPurposeRefinance


class MedianCLTVByTypeLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianCLTVByTypeLoanPurposeRefinance.getRoute
  val routeSummary = MedianCLTVByTypeLoanPurposeRefinance.getSummary
  "median cltv by type loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median cltv by type loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
