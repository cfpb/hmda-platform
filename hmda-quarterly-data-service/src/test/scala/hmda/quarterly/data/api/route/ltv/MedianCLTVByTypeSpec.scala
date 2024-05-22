package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.ltv.MedianCLTVByType


class MedianCLTVByTypeSpec extends WordSpec with Matchers {
  val route = MedianCLTVByType.getRoute
  val routeSummary = MedianCLTVByType.getSummary
  "median cltv by type route" should {
    "return the correct summary route" in {
      assert(!routeSummary.isCompleted)
    }
  }
  "median cltv by type route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
