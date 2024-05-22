//package hmda.quarterly.data.api.route
//
//import org.scalatest.{Matchers, WordSpec}
//import hmda.quarterly.data.api.route.rates.tlc.MedianTotalLoanCostsFHAByRaceLoanPurposeHome
//
//
//class MedianTotalLoanCostsFHAByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
//  val route = MedianTotalLoanCostsFHAByRaceLoanPurposeHome.getRoute
//  val routeSummary = MedianTotalLoanCostsFHAByRaceLoanPurposeHome.getSummary
//  "median total loan costs fha by race loan purpose home route" should {
//    "return the correct summary route" in {
//      assert(routeSummary.isCompleted)
//    }
//  }
//  "median total loan costs fha by race loan purpose home route" should {
//    "have a string title" in {
//      assert(route.title.isInstanceOf[String])
//    }
//  }
//}
