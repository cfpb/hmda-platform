package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVCCByRace


class MedianCLTVCCByRaceSpec extends WordSpec with Matchers {
  val route = MedianCLTVCCByRace.getRoute
  val routeSummary = MedianCLTVCCByRace.getSummary
  "median cltvcc by race route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median cltvcc by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
