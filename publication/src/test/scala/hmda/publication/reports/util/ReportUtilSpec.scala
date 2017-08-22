package hmda.publication.reports.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LarGenerators
import hmda.model.publication.reports.MSAReport
import hmda.publication.reports.util.ReportUtil._
import hmda.query.repository.filing.LarConverter._
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class ReportUtilSpec extends AsyncWordSpec with MustMatchers with LarGenerators {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  "msaReport" must {
    "get MSA info for given fips code and return MSAReport object" in {
      msaReport("10380") mustBe MSAReport("10380", "Aguadilla-Isabela, PR", "PR", "Puerto Rico")
      msaReport("26980") mustBe MSAReport("26980", "Iowa City, IA", "IA", "Iowa")
      msaReport("41100") mustBe MSAReport("41100", "St. George, UT", "UT", "Utah")
    }

    "Return empty MSAReport if fips code is invalid" in {
      msaReport("bogus") mustBe MSAReport("", "", "", "")
    }
  }

  "calculateMedianIncomeIntervals" must {
    "get median income info for given fips, convert to thousands" in {
      calculateMedianIncomeIntervals(10380)(2) mustBe 18.267
      calculateMedianIncomeIntervals(26980)(2) mustBe 81.027
      calculateMedianIncomeIntervals(41100)(2) mustBe 58.145
    }
    "return array of 50%, 80%, 100%, and 120% levels of median income (in thousands)" in {
      def roundTo3(v: Double) = BigDecimal(v).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
      calculateMedianIncomeIntervals(26980) mustBe Array(40.5135, 64.8216, 81.027, 97.2324)
      calculateMedianIncomeIntervals(41100).map(roundTo3) mustBe Array(29.073, 46.516, 58.145, 69.774)
    }
  }

  "calculateYear" must {
    "given a larSource, return the Activity Year for the LARs in it" in {
      // Activity Year is the same year as Action Taken Date. This is enforced by edit S270
      val lars = larListGen.sample.get.map(_.copy(actionTakenDate = 20090822))
      val src = Source.fromIterator(() => lars.toIterator)
        .map(lar => toLoanApplicationRegisterQuery(lar))

      calculateYear(src).map(_ mustBe 2009)
    }
  }

}
