package hmda.quarterly.data.api.route

import org.scalatest.{Matchers, WordSpec}
import hmda.quarterly.data.api.route.rates.dti.MedianDTICCByRaceLoanPurposeHome


class MedianDTICCByRaceLoanPurposeHomeSpec extends WordSpec with Matchers {
  val route = MedianDTICCByRaceLoanPurposeHome.getRoute
  val routeSummary = MedianDTICCByRaceLoanPurposeHome.getSummary
  "median dticc by race loan purpose home route" should {
    "return the correct summary route" in {
      assert(routeSummary.isCompleted)
    }
  }
  "median dticc by race loan purpose home route" should {
    "have a string title" in {
      assert(route.title.isInstanceOf[String])
    }
  }
}
