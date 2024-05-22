package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesFHAByRace


class DenialRatesFHAByRaceSpec extends WordSpec with Matchers {
  val route = DenialRatesFHAByRace.getRoute
  val routeSummary = DenialRatesFHAByRace.getSummary
  "denial rates fha by race route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "denial rates fha by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
