package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesCCByRace


class DenialRatesCCByRaceSpec extends WordSpec with Matchers {
  val route = DenialRatesCCByRace.getRoute
  val routeSummary = DenialRatesCCByRace.getSummary
  "denial rates cc by race route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "denial rates cc by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
