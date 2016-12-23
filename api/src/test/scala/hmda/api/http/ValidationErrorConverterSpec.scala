package hmda.api.http

import hmda.api.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import hmda.validation.engine.ts.TsEngine
import org.scalatest.{ MustMatchers, WordSpec }

class ValidationErrorConverterSpec extends WordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  "Validation errors" must {
    "be converted to edit check summary" in {
      val badLars: Seq[LoanApplicationRegister] = fiCSVEditErrors.split("\n").tail.map(line => LarCsvParser(line).right.get)
      val ctx = ValidationContext(None, Some(2017))
      val larErrors = badLars.flatMap(lar => validationErrors(lar, ctx, validateLar).errors)

      val tsErrors = Seq(SyntacticalValidationError("1299422144", "S020"), SyntacticalValidationError("1299422144", "S100"))

      val syntacticalEditResults =
        validationErrorsToEditResults(tsErrors, larErrors, Syntactical)
      val validityEditResults =
        validationErrorsToEditResults(tsErrors, larErrors, Validity)
      val qualityEditResults =
        validationErrorsToEditResults(tsErrors, larErrors, Quality)
      val macroEditResults =
        validationErrorsToMacroResults(larErrors)
      val summaryEditResults = SummaryEditResults(syntacticalEditResults, validityEditResults, qualityEditResults, macroEditResults)

      val s020 = EditResult("S020", "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code.", Seq(LarEditResult(LarId("Transmittal Sheet")), LarEditResult(LarId("8299422144")), LarEditResult(LarId("2185751599"))))
      val s010 = EditResult("S010", "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR).", Seq(LarEditResult(LarId("2185751599"))))
      val s100 = EditResult("S100", "Activity year must = year being processed (= 2017).", Seq(LarEditResult(LarId("Transmittal Sheet"))))

      summaryEditResults.syntactical.edits.head mustBe s020
      summaryEditResults.syntactical.edits.tail.contains(s010) mustBe true
      summaryEditResults.syntactical.edits.tail.contains(s100) mustBe true
      summaryEditResults.validity.edits.size mustBe 3
      summaryEditResults.quality mustBe EditResults(Nil)
      summaryEditResults.`macro` mustBe MacroResults(Nil)

    }
  }

}
