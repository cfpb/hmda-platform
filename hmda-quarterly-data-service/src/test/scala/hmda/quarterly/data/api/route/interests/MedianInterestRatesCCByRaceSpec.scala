package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.interests.MedianInterestRatesCCByRace


class MedianInterestRatesCCByRaceSpec extends WordSpec with Matchers {
  val route = MedianInterestRatesCCByRace.getRoute
  val routeSummary = MedianInterestRatesCCByRace.getSummary
  "median interest rates cc by race route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median interest rates cc by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
