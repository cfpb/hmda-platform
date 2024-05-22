package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTIFHAByRace


class MedianDTIFHAByRaceSpec extends WordSpec with Matchers {
  val route = MedianDTIFHAByRace.getRoute
  val routeSummary = MedianDTIFHAByRace.getSummary
  "median dtifha by race route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median dtifha by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
