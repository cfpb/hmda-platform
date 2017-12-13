package hmda.api.http

import hmda.api.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import hmda.model.validation._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsDatParser
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarEngine
import org.scalatest.{ MustMatchers, WordSpec }
import spray.json.{ JsNumber, JsObject }

class ValidationErrorConverterSpec extends WordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  "Validation errors" must {
    val ts: TransmittalSheet = TsDatParser(tsDAT)
    val badLars: Seq[LoanApplicationRegister] = fiCSVEditErrorsWithMsa.split("\n").tail.map(line => LarCsvParser(line).right.get)

    val tsErrors = Seq(
      SyntacticalValidationError("1299422144", "S020", true),
      SyntacticalValidationError("1299422144", "S100", true)
    )
    val larErrors: Seq[ValidationError] = {
      val ctx = ValidationContext(None, Some(2017))
      badLars.flatMap(lar => validationErrors(lar, ctx, validateLar).errors)
    }
    val macroMsaError: Seq[MacroValidationError] = Seq(MacroValidationError("Q029"))

    val validationState = HmdaFileValidationState(
      Some(ts),
      tsErrors,
      Nil,
      Nil,
      larErrors,
      Nil,
      Nil,
      qualityVerified = true,
      macroMsaError,
      macroVerified = false
    )

    val s020Desc = "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code."
    val s100Desc = "Activity year must = year being processed (= 2017)."

    "get descriptions for a collection of edits, removing duplicates" in {
      val infos: Seq[EditInfo] = editInfos(larErrors)
      infos.size mustBe larErrors.map(_.ruleName).distinct.size

      val s020 = infos.find(i => i.edit == "S020").get
      s020 mustBe EditInfo("S020", s020Desc)
    }

    "order edit info entries by rule name" in {
      val errors: Seq[ValidationError] = Seq(
        SyntacticalValidationError("", "S610", true),
        SyntacticalValidationError("", "S010", false),
        SyntacticalValidationError("", "S310", true),
        SyntacticalValidationError("", "S110", false),
        QualityValidationError("", "Q012", false),
        QualityValidationError("", "Q010", true),
        QualityValidationError("", "Q440", false),
        MacroValidationError("M000")
      )

      val result = editInfos(errors)

      result.map(_.edit) mustBe Seq(
        "M000",
        "Q010",
        "Q012",
        "Q440",
        "S010",
        "S110",
        "S310",
        "S610"
      )

    }

    "get edit info for validationErrors" in {
      val infos = editInfos(tsErrors)
      infos.size mustBe tsErrors.size
      infos.head mustBe EditInfo("S020", s020Desc)
      infos.last mustBe EditInfo("S100", s100Desc)
    }

    "have correct edit descriptions" in {
      val edits = List(ValidityValidationError("1234", "V295", false), ValidityValidationError("1235", "V300", false))
      val infos = editInfos(edits)
      infos.head mustBe EditInfo("V295", "State and county must = a valid combination or (county = NA where MSA/MD = NA). Valid state code format must be NN. Valid county code format must be NNN or NA.")
      infos.last mustBe EditInfo("V300", "Census tract must = a valid census tract number for the MSA/MD, state, county combination or (NA if county is classified as small) or (where MSA/MD = NA the census tract must = a valid census tract for the state/county combination or NA). Valid census tract format must be NNNN.NN or NA. Valid state code format must be NN. Valid county code format must be NNN or NA.")
    }

    "get msa info for Q029" in {
      val errorQ029 = QualityValidationError("8299422144", "Q029", ts = false)
      val result = validationErrorToResultRow(errorQ029, validationState)
      val msaField = result.fields.getFields("Metropolitan Statistical Area / Metropolitan Division Name").head.toString
      msaField mustBe "\"Battle Creek, MI\""
    }

    "convert edit to EditResultRow" in {
      val err = tsErrors.head
      val result = validationErrorToResultRow(err, validationState)
      result mustBe EditResultRow(
        RowId("Transmittal Sheet"),
        JsObject("Agency Code" -> JsNumber(9))
      )
    }

    "convert edits to CSV" in {
      val csvResults: Seq[String] = validationErrorsToCsvResults(validationState).split("\n")

      csvResults.length mustBe 18
      csvResults.head mustBe "editType, editId, loanId"
      csvResults(1) mustBe "Syntactical, S020, Transmittal Sheet"
      csvResults.last mustBe "Macro, Q029, "

    }
  }

}
