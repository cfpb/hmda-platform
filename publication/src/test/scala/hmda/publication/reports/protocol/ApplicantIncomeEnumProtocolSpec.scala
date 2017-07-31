package hmda.publication.reports.protocol

import hmda.model.publication.reports.ApplicantIncomeEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class ApplicantIncomeEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ApplicantIncomeEnumProtocol {

  property("Applicant Income Enum must convert to and from JSON") {
    forAll(applicantIncomeEnumGen) { a =>
      a.toJson.convertTo[ApplicantIncomeEnum] mustBe a
    }
  }
}
