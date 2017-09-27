package hmda.publication.reports.disclosure

import hmda.publication.reports.protocol.disclosure.D53Protocol._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import DisclosureReportGenerators._

class D53ProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("D53 Report must convert to and from JSON") {
    forAll(d53Gen) { d53 =>
      d53.toJson.convertTo[D53] mustBe d53
    }
  }

  property("D53 Report must serialize to the correct JSON format") {
    forAll(d53Gen) { d53 =>
      d53.toJson mustBe JsObject(
        "respondentId" -> JsString(d53.respondentId),
        "institutionName" -> JsString(d53.institutionName),
        "table" -> JsString("5-3"),
        "type" -> JsString("Disclosure"),
        "desc" -> JsString("Disposition of Applications to Refinance Loans on 1-to-4 Family and Manufactured Home Dwellings, by Income, Race, and Ethnicity of Applicant"),
        "year" -> JsNumber(d53.year),
        "reportDate" -> JsString(d53.reportDate),
        "msa" -> d53.msa.toJson,
        "applicantIncomes" -> d53.applicantIncomes.toJson,
        "total" -> d53.total.toJson
      )
    }
  }
}
