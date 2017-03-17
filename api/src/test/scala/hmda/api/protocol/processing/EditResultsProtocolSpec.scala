package hmda.api.protocol.processing

import hmda.api.model._
import hmda.model.fi.ValidatedWithErrors
import hmda.validation.engine.MacroEditJustification
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class EditResultsProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with EditResultsProtocol {

  property("EditResults must convert to and from json") {
    forAll(editResultsGen) { e =>
      e.toJson.convertTo[EditResults] mustBe e
    }
  }

  property("Summary edit results must convert to and from json") {
    forAll(summaryEditResultsGen) { s =>
      s.toJson.convertTo[SummaryEditResults] mustBe s
    }
  }

  val macroResult = MacroResult("Q888", Set(MacroEditJustification(1, "justified 1", false)))
  val expectedMacroJson = JsObject(
    ("edit", JsString("Q888")),
    ("justifications", JsArray(
      JsObject(
        ("id", JsNumber(1)),
        ("value", JsString("justified 1")),
        ("verified", JsBoolean(false))
      )
    ))
  )
  property("MacroResults must have proper json format") {
    macroResult.toJson mustBe expectedMacroJson
  }

  property("QualityEditsVerifiedResponse must have correct json format") {
    val response = QualityEditsVerifiedResponse(true, ValidatedWithErrors)
    response.toJson mustBe JsObject(
      ("verified", JsBoolean(true)),
      ("status", JsObject(
        ("code", JsNumber(8)),
        ("message", JsString("validated with errors"))
      ))
    )
  }
}
