package hmda.publication.reports.disclosure

import hmda.publication.reports.protocol.disclosure.D51Protocol._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import DisclosureReportGenerators._

class D51ProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("D51 Report must convert to and from JSON") {
    forAll(d51Gen) { d51 =>
      d51.toJson.convertTo[D51] mustBe d51
    }
  }

  property("D51 Report must serialize to the correct JSON format") {
    forAll(d51Gen) { d51 =>
      d51.toJson mustBe JsObject(
        "respondentId" -> JsString(d51.respondentId),
        "institutionName" -> JsString(d51.institutionName),
        "table" -> JsString("5-1"),
        "type" -> JsString("Disclosure"),
        "desc" -> JsString("Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant"),
        "year" -> JsNumber(d51.year),
        "reportDate" -> JsString(d51.reportDate),
        "msa" -> d51.msa.toJson,
        "applicantIncomes" -> d51.applicantIncomes.toJson,
        "total" -> d51.total.toJson
      )
    }
  }
}
