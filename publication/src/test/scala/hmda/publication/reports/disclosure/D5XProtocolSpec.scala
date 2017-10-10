package hmda.publication.reports.disclosure

import hmda.publication.reports.protocol.disclosure.D5XProtocol._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import DisclosureReportGenerators._

class D5XProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("D5X Report must convert to and from JSON") {
    forAll(d5XGen) { d5X =>
      d5X.toJson.convertTo[D5X] mustBe d5X
    }
  }

  property("D5X Report must serialize to the correct JSON format") {
    forAll(d5XGen) { d5X =>
      d5X.toJson mustBe JsObject(
        "respondentId" -> JsString(d5X.respondentId),
        "institutionName" -> JsString(d5X.institutionName),
        "table" -> JsString(d5X.table),
        "type" -> JsString("Disclosure"),
        "desc" -> JsString(d5X.description),
        "year" -> JsNumber(d5X.year),
        "reportDate" -> JsString(d5X.reportDate),
        "msa" -> d5X.msa.toJson,
        "applicantIncomes" -> d5X.applicantIncomes.toJson,
        "total" -> d5X.total.toJson
      )
    }
  }
}
