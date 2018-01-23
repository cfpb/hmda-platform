package hmda.api.http

import akka.NotUsed
import akka.actor.ActorSystem

import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.api.model._
import hmda.model.fi.SubmissionId
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import hmda.model.validation._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsDatParser
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.validation.engine.lar.LarEngine
import org.scalatest.{ AsyncWordSpec, MustMatchers }
import spray.json.{ JsNumber, JsObject }

class ValidationErrorConverterSpec extends AsyncWordSpec with MustMatchers with ValidationErrorConverter with LarEngine {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val editEvents: List[Event] = List(
    TsSyntacticalError(SyntacticalValidationError("xyz", "S013", true)),
    LarSyntacticalError(SyntacticalValidationError("xyz", "S205", false)),
    LarValidityError(ValidityValidationError("xyz", "V210", false)),
    TsValidityError(ValidityValidationError("xyz", "V145", true)),
    TsQualityError(QualityValidationError("xyz", "Q595", true)),
    TsQualityError(QualityValidationError("xyz", "Q595", true)),
    TsQualityError(QualityValidationError("xyz", "Q130", true)),
    LarQualityError(QualityValidationError("xyz", "Q037", false)),
    LarQualityError(QualityValidationError("xyz", "Q037", false)),
    LarMacroError(MacroValidationError("Q083")),
    EditsVerified(Quality, true),
    EditsVerified(Macro, false)
  )

  val eventSource: Source[Event, NotUsed] = Source.fromIterator(() => editEvents.toIterator)

  "Edits Collection" must {

    "filter for syntactical edits from an event source" in {
      val first: Source[ValidationError, NotUsed] = editStreamOfType("syntactical", eventSource)
      val syntacticalF: Future[Seq[ValidationError]] = first.runWith(Sink.seq)
      syntacticalF.map(result => result must have size 2)
    }

    "filter for quality edits from an event source" in {
      val first: Source[ValidationError, NotUsed] = editStreamOfType("quality", eventSource)
      val qualityF: Future[Seq[ValidationError]] = first.runWith(Sink.seq)
      qualityF.map(result => result must have size 5)
    }

    "gather Edit Info for each relevant edit, without duplicates" in {
      val infosF: Future[List[EditInfo]] = editInfosF("quality", eventSource)
      infosF.map { result =>
        result must have size 3
        result.head mustBe EditInfo("Q037", "If lien status = 2, then loan amount should be ≤ $250 ($250 thousand).")
        result(1) mustBe EditInfo("Q130", "The number of loan/application records received in this transmission file per respondent does not = the total number of loan/application records reported in this respondent’s transmission or the total number of loan/application records in this submission is missing from the transmittal sheet.")
      }
    }

    "order edit info entries by rule name" in {
      editInfosF("quality", eventSource).map { result =>
        result.map(_.edit) mustBe Seq("Q037", "Q130", "Q595")
      }
    }

  }

  "Edits CSV response" must {
    "gather all edits from event stream" in {
      val first: Source[ValidationError, NotUsed] = editStreamOfType("all", eventSource)
      val syntacticalF: Future[Seq[ValidationError]] = first.runWith(Sink.seq)
      syntacticalF.map(result => result must have size 10)
    }

    "convert to csv, add header row" in {
      val csvLinesF = csvResultStream(eventSource).runWith(Sink.seq)
      csvLinesF.map { csvLines =>
        csvLines must have size 11
        csvLines.head mustBe "editType, editId, loanId"
        csvLines(1) mustBe "Syntactical, S013, Transmittal Sheet"
        csvLines(2) mustBe "Syntactical, S205, xyz"
        csvLines.last mustBe "Macro, Q083, "
      }
    }
  }

  "Validation errors" must {
    val ts: TransmittalSheet = TsDatParser(tsDAT)
    val larEvents: Seq[Event] =
      fiCSVEditErrorsWithMsa.split("\n").tail.map { line =>
        LarValidated(LarCsvParser(line).right.get, SubmissionId())
      }
    val larEventSource: Source[Event, NotUsed] = Source.fromIterator(() => larEvents.toIterator)

    val tsErrors = Seq(
      SyntacticalValidationError("1299422144", "S020", true),
      SyntacticalValidationError("1299422144", "S100", true)
    )

    "get msa info for Q029" in {
      val errorQ029 = QualityValidationError("8299422144", "Q029", ts = false)
      val resultF = validationErrorToResultRow(errorQ029, Some(ts), larEventSource)
      resultF.map { result =>
        val msaField = result.fields.getFields("Metropolitan Statistical Area / Metropolitan Division Name").head.toString
        msaField mustBe "\"Battle Creek, MI\""
      }
    }

    "convert edit to EditResultRow" in {
      val resultF = validationErrorToResultRow(tsErrors.head, Some(ts), larEventSource)
      resultF.map { result =>
        result mustBe EditResultRow(
          RowId("Transmittal Sheet"),
          JsObject("Agency Code" -> JsNumber(9))
        )
      }
    }
  }

}
