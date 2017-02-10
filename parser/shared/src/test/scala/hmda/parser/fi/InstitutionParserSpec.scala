package hmda.parser.fi

import hmda.model.institution.Agency.FRS
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class InstitutionParserSpec extends PropSpec with PropertyChecks with MustMatchers {
  val testInst = "2015|123.0|2|2|1|1234567.0|123|123.0|-1.0|-1.0||||" +
    "FIRST BANK|VA|CITY|2|0|-1.0|12346578.0|" +
    "FIRST BANK|CITY|VA|12345.0|0|-1.0||||"

  property("Institution parser must properly convert a string to an Institution object") {
    InstitutionParser(testInst).toString mustBe
      Institution("123", FRS, 2015, Bank, cra = true,
        Set(
          ExternalId("123.0", FdicCertNo),
          ExternalId("123", RssdId),
          ExternalId("-1.0", NcuaCharterId),
          ExternalId("1234567.0", FederalTaxId),
          ExternalId("-1.0", OccCharterId)
        ), Set(),
        Respondent(ExternalId("123.0", RssdId), "FIRST BANK", "VA", "CITY", "2"),
        hmdaFilerFlag = false,
        Parent("-1.0", -1, "FIRST BANK", "CITY", "VA"),
        -1, 0,
        TopHolder(-1, "", "", "", "")).toString
  }
}
