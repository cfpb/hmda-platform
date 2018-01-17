package hmda.api.http

import akka.NotUsed
import akka.actor.ActorSystem

import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.api.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import hmda.model.validation._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsDatParser
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarEngine
import org.scalatest.{ AsyncWordSpec, MustMatchers }
import spray.json.{ JsNumber, JsObject }

class ValidationErrorConverterSpec extends AsyncWordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  ///// New way /////

  "Validation Error Converter" must {
    val events: List[Event] = List(
      LarSyntacticalError(SyntacticalValidationError("xyz", "S205", false)),
      TsSyntacticalError(SyntacticalValidationError("xyz", "S013", true)),
      LarValidityError(ValidityValidationError("xyz", "V210", false)),
      TsValidityError(ValidityValidationError("xyz", "V145", true)),
      LarQualityError(QualityValidationError("xyz", "Q037", false)),
      LarQualityError(QualityValidationError("xyz", "Q037", false)),
      TsQualityError(SyntacticalValidationError("xyz", "Q130", true)),
      LarMacroError(MacroValidationError("Q083"))
    )

    val eventSource: Source[Event, NotUsed] = Source.fromIterator(() => events.toIterator)

    "filter for syntactical edits from an event source" in {
      val first: Source[ValidationError, NotUsed] = editStreamOfType("syntactical", eventSource)
      val syntacticalF: Future[Seq[ValidationError]] = first.runWith(Sink.seq)
      syntacticalF.map(result => result must have size 2)
    }

    "filter for quality edits from an event source" in {
      val first: Source[ValidationError, NotUsed] = editStreamOfType("quality", eventSource)
      val qualityF: Future[Seq[ValidationError]] = first.runWith(Sink.seq)
      qualityF.map(result => result must have size 3)
    }

    "gather Edit Info for each relevant edit, without duplicates" in {
      val infosF: Future[List[EditInfo]] = editInfosF("quality", eventSource)
      infosF.map { result =>
        result must have size 2
        result.head mustBe EditInfo("Q037", "If lien status = 2, then loan amount should be ≤ $250 ($250 thousand).")
        result(1) mustBe EditInfo("Q130", "The number of loan/application records received in this transmission file per respondent does not = the total number of loan/application records reported in this respondent’s transmission or the total number of loan/application records in this submission is missing from the transmittal sheet.")
      }
    }
  }

  ////// Old way /////

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
      badLars,
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

      csvResults.length mustBe 15
      csvResults.head mustBe "editType, editId, loanId"
      csvResults(1) mustBe "Syntactical, S020, Transmittal Sheet"
      csvResults.last mustBe "Macro, Q029, "

    }
  }

}
