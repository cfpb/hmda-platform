package hmda.api.protocol.processing

import hmda.api.model._
import hmda.model.fi.ValidatedWithErrors
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class EditResultsProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with EditResultsProtocol {

  property("EditCollection must convert to and from json") {
    forAll(editCollectionGen) { e =>
      e.toJson.convertTo[EditCollection] mustBe e
    }
  }

  property("VerifiableEditCollection must convert to and from json") {
    forAll(verifiableEditCollectionGen) { v =>
      v.toJson.convertTo[VerifiableEditCollection] mustBe v
    }
  }

  property("Summary edit results must convert to and from json") {
    forAll(summaryEditResultsGen) { s =>
      s.toJson.convertTo[SummaryEditResults] mustBe s
    }
  }

  property("QualityEditsVerifiedResponse must have correct json format") {
    val response = EditsVerifiedResponse(true, ValidatedWithErrors)
    response.toJson mustBe JsObject(
      ("verified", JsBoolean(true)),
      ("status", JsObject(
        ("code", JsNumber(8)),
        ("message", JsString("validated with errors"))
      ))
    )
  }
}
