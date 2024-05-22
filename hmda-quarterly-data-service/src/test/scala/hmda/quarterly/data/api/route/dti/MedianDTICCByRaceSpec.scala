package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTICCByRace


class MedianDTICCByRaceSpec extends WordSpec with Matchers {
  val route = MedianDTICCByRace.getRoute
  val routeSummary = MedianDTICCByRace.getSummary
  "median dticc by race route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median dticc by race route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
