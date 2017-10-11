package hmda.publication.reports.national

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import NationalAggregateReportGenerators._
import hmda.publication.reports.protocol.national.N52Protocol._

class N52ProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("N52 Report must convert to and from JSON") {
    forAll(n52Gen) { n52 =>
      n52.toJson.convertTo[N52] mustBe n52
    }
  }

  property("N52 Report must serialize to the correct JSON format") {
    forAll(n52Gen) { n52 =>
      n52.toJson mustBe JsObject(
        "table" -> JsString("5-2"),
        "type" -> JsString("NationalAggregate"),
        "desc" -> JsString("Disposition of applications for conventional home purchase loans on 1-to-4 family and manufactured home dwellings"),
        "year" -> JsNumber(n52.year),
        "reportDate" -> JsString(n52.reportDate),
        "applicantIncomes" -> n52.applicantIncomes.toJson,
        "total" -> n52.total.toJson
      )
    }
  }
}

