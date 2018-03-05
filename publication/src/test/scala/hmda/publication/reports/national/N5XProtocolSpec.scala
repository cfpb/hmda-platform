package hmda.publication.reports.national

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import NationalAggregateReportGenerators._
import hmda.publication.reports.protocol.national.N5XProtocol._

class N5XProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("N5X Report must convert to and from JSON") {
    forAll(n5XGen) { n5X =>
      n5X.toJson.convertTo[N5X] mustBe n5X
    }
  }

  property("N5X Report must serialize to the correct JSON format") {
    forAll(n5XGen) { n5X =>
      n5X.toJson mustBe JsObject(
        "table" -> JsString(n5X.table),
        "type" -> JsString("National Aggregate"),
        "desc" -> JsString(n5X.description),
        "year" -> JsNumber(n5X.year),
        "reportDate" -> JsString(n5X.reportDate),
        "applicantIncomes" -> n5X.applicantIncomes.toJson,
        "total" -> n5X.total.toJson
      )
    }
  }
}

