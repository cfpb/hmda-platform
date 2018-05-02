package hmda.publication.reports.util

import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import org.scalatest.{ MustMatchers, WordSpec }

class ReportsMetaDataLookupSpec extends WordSpec with MustMatchers {

  "Provide A&D report metadata" in {
    val d51 = ReportsMetaDataLookup.values("D51")
    d51 mustBe a[ReportMetaData]
    d51.reportType mustBe Disclosure
    d51.reportTable mustBe "5-1"
  }

}
