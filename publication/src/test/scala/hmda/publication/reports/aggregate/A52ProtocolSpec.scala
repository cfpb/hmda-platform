package hmda.publication.reports.aggregate

import hmda.publication.reports.protocol.aggregate.A52Protocol._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._
import AggregateReportGenerators._

class A52ProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("A52 Report must convert to and from JSON") {
    forAll(a52Gen) { a52 =>
      a52.toJson.convertTo[A52] mustBe a52
    }
  }

  property("A52 Report must serialize to the correct JSON format") {
    forAll(a52Gen) { a52 =>
      a52.toJson mustBe JsObject(
        "table" -> JsString("5-2"),
        "type" -> JsString("Aggregate"),
        "desc" -> JsString("Disposition of Applications for Conventional Home-Purchase Loans, 1-to-4 Family and Manufactured Home Dwellings, by Income, Race, and Ethnicity of Applicant"),
        "year" -> JsNumber(a52.year),
        "reportDate" -> JsString(a52.reportDate),
        "msa" -> a52.msa.toJson,
        "applicantIncomes" -> a52.applicantIncomes.toJson,
        "total" -> a52.total.toJson
      )
    }
  }
}
