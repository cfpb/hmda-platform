package hmda.api.protocol.processing

import hmda.api.model.{ FilingDetail, InstitutionSummary, ModelGenerators }
import hmda.model.fi.Filing
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class FilingProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with FilingProtocol {

  property("Filing must convert to and from json") {
    forAll(filingGen) { f =>
      f.toJson.convertTo[Filing] mustBe f
    }
  }

  property("Filing detail must convert to and from json") {
    forAll(filingDetailGen) { f =>
      f.toJson.convertTo[FilingDetail] mustBe f
    }
  }

  property("An Institution Summary must convert to and from json") {
    forAll(institutionsSummaryGen) { s =>
      s.toJson.convertTo[InstitutionSummary] mustBe s
    }
  }

  implicit val institutionsSummaryGen: Gen[InstitutionSummary] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      filings <- Gen.listOf(filingGen)
    } yield InstitutionSummary(id, name, filings)
  }

}
