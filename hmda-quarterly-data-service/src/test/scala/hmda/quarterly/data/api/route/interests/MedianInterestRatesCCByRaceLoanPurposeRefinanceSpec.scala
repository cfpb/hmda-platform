//package hmda.quarterly.data.api.route
//
//import org.scalatest.{Matchers, WordSpec}
//import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesCCByRaceLoanPurposeRefinance
//
//
//class MedianInterestRatesCCByRaceLoanPurposeRefinanceSpec extends WordSpec with Matchers {
//  val route = MedianInterestRatesCCByRaceLoanPurposeRefinance.getRoute
//  val routeSummary = MedianInterestRatesCCByRaceLoanPurposeRefinance.getSummary
//  "median interest rates cc by race loan purpose refinance route" should {
//    "return the correct summary route" in {
//      assert(routeSummary.isCompleted)
//    }
//  }
//  "median interest rates cc by race loan purpose refinance route" should {
//    "have a string title" in {
//      assert(route.title.isInstanceOf[String])
//    }
//  }
//}
