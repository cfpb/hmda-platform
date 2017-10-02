package hmda.publication.reports.aggregate

import hmda.publication.reports.protocol.aggregate.A53Protocol._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._
import AggregateReportGenerators._

class A53ProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("A53 Report must convert to and from JSON") {
    forAll(a53Gen) { a53 =>
      a53.toJson.convertTo[A53] mustBe a53
    }
  }

  property("A53 Report must serialize to the correct JSON format") {
    forAll(a53Gen) { a53 =>
      a53.toJson mustBe JsObject(
        "table" -> JsString("5-3"),
        "type" -> JsString("Aggregate"),
        "desc" -> JsString("Disposition of Applications to Refinance Loans on 1-to-4 Family and Manufactured Home Dwellings, by Income, Race, and Ethnicity of Applicant"),
        "year" -> JsNumber(a53.year),
        "reportDate" -> JsString(a53.reportDate),
        "msa" -> a53.msa.toJson,
        "applicantIncomes" -> a53.applicantIncomes.toJson,
        "total" -> a53.total.toJson
      )
    }
  }
}
