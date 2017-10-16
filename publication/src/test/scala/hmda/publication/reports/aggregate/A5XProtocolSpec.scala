package hmda.publication.reports.aggregate

import hmda.publication.reports.protocol.aggregate.A5XProtocol._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._
import AggregateReportGenerators._

class A5XProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("A5X Report must convert to and from JSON") {
    forAll(a5XGen) { a5X =>
      a5X.toJson.convertTo[A5X] mustBe a5X
    }
  }

  property("A5X Report must serialize to the correct JSON format") {
    forAll(a5XGen) { a5X =>
      a5X.toJson mustBe JsObject(
        "table" -> JsString(a5X.table),
        "type" -> JsString("Aggregate"),
        "desc" -> JsString(a5X.description),
        "year" -> JsNumber(a5X.year),
        "reportDate" -> JsString(a5X.reportDate),
        "msa" -> a5X.msa.toJson,
        "applicantIncomes" -> a5X.applicantIncomes.toJson,
        "total" -> a5X.total.toJson
      )
    }
  }
}
