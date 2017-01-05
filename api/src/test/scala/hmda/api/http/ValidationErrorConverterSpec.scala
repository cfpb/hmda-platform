package hmda.api.http

import hmda.api.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.util.FITestData._
import hmda.parser.fi.lar.LarCsvParser
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import org.scalatest.{ MustMatchers, WordSpec }
import spray.json.{ JsBoolean, JsNumber, JsObject }

class ValidationErrorConverterSpec extends WordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  "Validation errors" must {

    val tsErrors = Seq(
      SyntacticalValidationError("1299422144", "S020", true),
      SyntacticalValidationError("1299422144", "S100", true)
    )

    val larErrors: Seq[ValidationError] = {
      val badLars: Seq[LoanApplicationRegister] = fiCSVEditErrors.split("\n").tail.map(line => LarCsvParser(line).right.get)
      val ctx = ValidationContext(None, Some(2017))
      badLars.flatMap(lar => validationErrors(lar, ctx, validateLar).errors)
    }

    val macroErrors: Seq[MacroValidationError] = Seq(MacroValidationError("Q047", Seq()))

    val s020Desc = "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code."
    val s010Desc = "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR)."
    val s100Desc = "Activity year must = year being processed (= 2017)."

    "be converted to edit check summary" in {
      val s020 = EditResult("S020", s020Desc, Seq(EditResultRow(RowId("Transmittal Sheet")), EditResultRow(RowId("8299422144")), EditResultRow(RowId("2185751599"))))
      val s010 = EditResult("S010", s010Desc, Seq(EditResultRow(RowId("2185751599"))))
      val s100 = EditResult("S100", s100Desc, Seq(EditResultRow(RowId("Transmittal Sheet"))))

      val syntacticalEditResults = validationErrorsToEditResults(tsErrors, larErrors, Syntactical)
      val validityEditResults = validationErrorsToEditResults(tsErrors, larErrors, Validity)
      val qualityEditResults = validationErrorsToEditResults(tsErrors, larErrors, Quality)
      val macroEditResults = validationErrorsToMacroResults(larErrors)
      val summaryEditResults = SummaryEditResults(syntacticalEditResults, validityEditResults, qualityEditResults, macroEditResults)

      summaryEditResults.syntactical.edits.head mustBe s020
      summaryEditResults.syntactical.edits.tail.contains(s010) mustBe true
      summaryEditResults.syntactical.edits.tail.contains(s100) mustBe true
      summaryEditResults.validity.edits.size mustBe 3
      summaryEditResults.quality mustBe EditResults(Nil)
      summaryEditResults.`macro` mustBe MacroResults(Nil)

    }

    "sort failures by row" in {
      val fields = JsObject(
        ("Thing One", JsNumber(3)),
        ("Thing Two", JsBoolean(false))
      )
      val tsResults = RowResult(
        "Transmittal Sheet",
        Seq(RowEditDetail("S020", s020Desc, fields), RowEditDetail("S100", s100Desc, fields))
      )
      val macros = MacroResult("Q047", Set(MacroEditJustification(1, "There were many requests for preapprovals, but the applicant did not proceed with the loan.", false)))

      val results: RowResults = validationErrorsToRowResults(tsErrors, larErrors, macroErrors)
      results.rows.size mustBe 4
      results.rows.head mustBe tsResults
      results.`macro`.edits.contains(macros) mustBe true

      val larRow = results.rows.find(_.rowId == "4977566612").get
      larRow.edits.size mustBe 3
      larRow.edits.head.editId mustBe "V550"
    }
  }

}
