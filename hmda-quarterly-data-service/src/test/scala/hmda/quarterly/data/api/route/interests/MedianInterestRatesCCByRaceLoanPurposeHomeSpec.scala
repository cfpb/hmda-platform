//package hmda.quarterly.data.api.route
//
//import org.scalatest.{Matchers, WordSpec}
//import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesCCByRaceLoanPurposeHome
//
//
//class MedianInterestRatesCCByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
//  val route = MedianInterestRatesCCByRaceLoanPurposeHome.getRoute
//  val routeSummary = MedianInterestRatesCCByRaceLoanPurposeHome.getSummary
//  "median interest rates cc by race loan purpose home route" should {
//    "return the correct summary route" in {
//      assert(!routeSummary.isCompleted)
//    }
//  }
//  "median interest rates cc by race loan purpose home route" should {
//    "have a string title" in {
//      assert(route.title.isInstanceOf[String])
//    }
//  }
//}
