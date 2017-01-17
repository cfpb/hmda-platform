package hmda.api.http

import hmda.api.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsDatParser
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import org.scalatest.{ MustMatchers, WordSpec }
import spray.json.{ JsBoolean, JsNumber, JsObject }

class ValidationErrorConverterSpec extends WordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  "Validation errors" must {
    val ts: TransmittalSheet = TsDatParser(tsDAT)
    val badLars: Seq[LoanApplicationRegister] = fiCSVEditErrors.split("\n").tail.map(line => LarCsvParser(line).right.get)

    val tsErrors = Seq(
      SyntacticalValidationError("1299422144", "S020", true),
      SyntacticalValidationError("1299422144", "S100", true)
    )
    val larErrors: Seq[ValidationError] = {
      val ctx = ValidationContext(None, Some(2017))
      badLars.flatMap(lar => validationErrors(lar, ctx, validateLar).errors)
    }
    val macroErrors: Seq[MacroValidationError] = Seq(MacroValidationError("Q047", Seq()))

    val validationState = HmdaFileValidationState(
      Some(ts),
      badLars,
      tsErrors,
      Nil,
      Nil,
      larErrors,
      Nil,
      Nil,
      macroErrors
    )

    val s020Desc = "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code."
    val s010Desc = "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR)."
    val s100Desc = "Activity year must = year being processed (= 2017)."

    "be converted to edit check summary" in {
      val syntacticalEditResults = validationErrorsToEditResults(validationState, tsErrors, larErrors, Syntactical)
      val validityEditResults = validationErrorsToEditResults(validationState, tsErrors, larErrors, Validity)
      val qualityEditResults = validationErrorsToEditResults(validationState, tsErrors, larErrors, Quality)
      val macroEditResults = validationErrorsToMacroResults(larErrors)
      val summaryEditResults = SummaryEditResults(syntacticalEditResults, validityEditResults, qualityEditResults, macroEditResults)

      summaryEditResults.syntactical.edits.head.edit mustBe "S020"
      summaryEditResults.syntactical.edits.head.description mustBe s020Desc
      summaryEditResults.syntactical.edits.head.rows.size mustBe 3

      summaryEditResults.syntactical.edits.tail.size mustBe 2

      summaryEditResults.validity.edits.size mustBe 3
      summaryEditResults.quality mustBe EditResults(Nil)
      summaryEditResults.`macro` mustBe MacroResults(Nil)

    }

    "sort failures by row" in {
      val macros = MacroResult("Q047", Set(MacroEditJustification(1, "There were many requests for preapprovals, but the applicant did not proceed with the loan.", false)))

      val results: RowResults = validationErrorsToRowResults(validationState, tsErrors, larErrors, macroErrors)
      results.rows.size mustBe 4
      results.`macro`.edits.contains(macros) mustBe true

      val tsRow = results.rows.head
      tsRow.rowId mustBe "Transmittal Sheet"
      tsRow.edits.size mustBe 2
      tsRow.edits.head.editId mustBe "S020"
      tsRow.edits.head.description mustBe s020Desc

      val larRow = results.rows.find(_.rowId == "4977566612").get
      larRow.edits.size mustBe 3
      larRow.edits.head.editId mustBe "V550"
    }
  }

}
