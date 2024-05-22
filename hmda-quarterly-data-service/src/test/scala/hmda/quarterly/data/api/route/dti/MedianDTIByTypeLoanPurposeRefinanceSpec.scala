package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTIByTypeLoanPurposeRefinance


class MedianDTIByTypeLoanPurposeRefinanceSpec extends WordSpec with Matchers {
  val route = MedianDTIByTypeLoanPurposeRefinance.getRoute
  val routeSummary = MedianDTIByTypeLoanPurposeRefinance.getSummary
  "median dti by type loan purpose refinance route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median dti by type loan purpose refinance route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
