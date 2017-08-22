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

}
