package hmda.publication.reports.protocol

import hmda.model.publication.reports.ReportTypeEnum
import hmda.publication.reports.ReportGenerators._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class ReportTypeEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ReportTypeEnumProtocol {

  property("Report Type must convert to and from JSON") {
    forAll(reportTypeGen) { r =>
      r.toJson.convertTo[ReportTypeEnum] mustBe r
    }
  }
}
