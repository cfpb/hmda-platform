//package hmda.quarterly.data.api.route
//
//import org.scalatest.{Matchers, WordSpec}
//import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesLoanPurposeRefinance
//
//
//class MedianInterestRatesLoanPurposeRefinanceSpec extends WordSpec with Matchers {
//  val route = MedianInterestRatesLoanPurposeRefinance.getRoute
//  val routeSummary = MedianInterestRatesLoanPurposeRefinance.getSummary
//  "median interest rates loan purpose refinance route" should {
//    "return the correct summary route" in {
//      assert(routeSummary.isCompleted)
//    }
//  }
//  "median interest rates loan purpose refinance route" should {
//    "have a string title" in {
//      assert(route.title.isInstanceOf[String])
//    }
//  }
//}
