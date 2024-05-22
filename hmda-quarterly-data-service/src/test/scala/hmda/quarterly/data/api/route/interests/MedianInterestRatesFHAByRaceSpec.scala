//package hmda.quarterly.data.api.route
//
//import org.scalatest.{Matchers, WordSpec}
//import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesFHAByRace
//
//
//class MedianInterestRatesFHAByRaceSpec extends WordSpec with Matchers {
//  val route = MedianInterestRatesFHAByRace.getRoute
//  val routeSummary = MedianInterestRatesFHAByRace.getSummary
//  "median interest rates fha by race route" should {
//    "return the correct summary route" in {
//      assert(routeSummary.isCompleted)
//    }
//  }
//  "median interest rates fha by race route" should {
//    "have a string title" in {
//      assert(route.title.isInstanceOf[String])
//    }
//  }
//}
