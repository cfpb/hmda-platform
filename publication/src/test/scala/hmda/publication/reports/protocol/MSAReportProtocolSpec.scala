package hmda.publication.reports.protocol

import hmda.model.publication.reports.MSAReport
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class MSAReportProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with MSAReportProtocol {

  property("MSA Report must convert to and from JSON") {
    forAll(msaReportGen) { m =>
      m.toJson.convertTo[MSAReport] mustBe m
    }
  }
}
