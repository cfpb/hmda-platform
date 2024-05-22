package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTIFHAByRaceLoanPurposeHome


class MedianDTIFHAByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianDTIFHAByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianDTIFHAByRaceLoanPurposeHome.getSummary
  "median dtifha by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median dtifha by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
