package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.denials.DenialRatesCCByRaceLoanPurposeHome


class DenialRatesCCByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = DenialRatesCCByRaceLoanPurposeHome.getRoute
  val routeSummary = DenialRatesCCByRaceLoanPurposeHome.getSummary
  "denial rates cc by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "denial rates cc by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
