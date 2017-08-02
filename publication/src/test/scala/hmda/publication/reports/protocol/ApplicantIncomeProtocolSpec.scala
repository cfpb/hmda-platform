package hmda.publication.reports.protocol

import hmda.model.publication.reports.ApplicantIncome
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class ApplicantIncomeProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ApplicantIncomeProtocol {

  property("Applicant Income must convert to and from JSON") {
    forAll(applicantIncomeGen) { a =>
      a.toJson.convertTo[ApplicantIncome] mustBe a
    }
  }
}
