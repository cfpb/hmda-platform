package hmda.publication.reports.util

import hmda.model.publication.reports.MSAReport
import hmda.publication.reports.util.ReportUtil._
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class ReportUtilSpec extends AsyncWordSpec with MustMatchers {

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

}
