package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTIByType


class MedianDTIByTypeSpec extends WordSpec with Matchers {
  val route = MedianDTIByType.getRoute
  val routeSummary = MedianDTIByType.getSummary
  "median dti by type route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median dti by type route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
