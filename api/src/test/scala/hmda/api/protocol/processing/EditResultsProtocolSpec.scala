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

  val fields = JsObject(("Thing One", JsNumber(1)), ("Thing Two", JsString("two")))
  val rowDetail = RowEditDetail("V111", "the values must be correct", fields)
  val expectedDetailJson = JsObject(
    ("editId", JsString("V111")),
    ("description", JsString("the values must be correct")),
    ("fields", fields)
  )
  property("Row Edit Detail must have proper json format") {
    rowDetail.toJson mustBe expectedDetailJson
  }

  val rowResult = RowResult("lar55", Seq(rowDetail))
  val expectedRowJson = JsObject(
    ("rowId", JsString("lar55")),
    ("edits", JsArray(expectedDetailJson))
  )
  property("RowResult must have proper json format") {
    rowResult.toJson mustBe expectedRowJson
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

  property("RowResults must have proper json format") {
    val macros = MacroResults(Seq(macroResult))
    val rows = RowResults(Seq(rowResult), macros)
    val expectedRowsJson = JsObject(
      ("rows", JsArray(expectedRowJson)),
      ("macro", JsObject(
        ("edits", JsArray(expectedMacroJson))
      ))
    )
    rows.toJson mustBe expectedRowsJson
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
