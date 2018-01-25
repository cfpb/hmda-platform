package hmda.api.http

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.api._
import hmda.api.model._
import hmda.census.model.CbsaLookup
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.fi.HmdaFileRow
import hmda.model.validation.{ EmptyValidationError, ValidationError }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.util.SourceUtils
import spray.json.{ JsNumber, JsObject, JsString }

import scala.concurrent.Future

trait ValidationErrorConverter extends SourceUtils {

  ///// Edit Collection
  def editInfos[ec: EC, mat: MAT, as: AS](editNames: Set[String]): List[EditInfo] = {
    editNames.toList.sorted.map(name => EditInfo(name, editDescription(name)))
  }

  private def editDescription(editName: String): String = {
    EditMetaDataLookup.forEdit(editName).editDescription
  }

  ///// Edits Report CSV
  private val csvHeaderSource = Source.fromIterator(() => Iterator("editType, editId, loanId\n"))

  def csvResultStream[ec: EC, mat: MAT, as: AS](eventSource: Source[Event, NotUsed]): Source[String, Any] = {
    val edits = allEdits(eventSource)
    val csvSource = edits.map(_.toCsv)
    csvHeaderSource.concat(csvSource)
  }

  def allEdits(eventSource: Source[Event, NotUsed]) = {
    val edits: Source[ValidationError, NotUsed] =
      eventSource.map {
        case LarSyntacticalError(err) => err
        case TsSyntacticalError(err) => err
        case LarValidityError(err) => err
        case TsValidityError(err) => err
        case LarQualityError(err) => err
        case TsQualityError(err) => err
        case LarMacroError(err) => err
        case _ => EmptyValidationError
      }

    edits.filter(_ != EmptyValidationError)
  }

  ///// Edit Details
  def resultRowsFromCollection[ec: EC, mat: MAT, as: AS](errors: Seq[ValidationError], ts: Option[TransmittalSheet], eventSource: Source[Event, NotUsed]): Future[Seq[EditResultRow]] = {
    val fieldNames = fieldNamesForEdit(errors.head)
    val rowIds = errors.map(_.errorId)

    val tsRows = tsRow(errors, ts, fieldNames)
    val larRowsF: Future[Seq[EditResultRow]] = relevantLars(rowIds, eventSource).map { lar =>
      EditResultRow(RowId(lar.loan.id), fieldJsonForRow(fieldNames, lar))
    }.runWith(Sink.seq)

    larRowsF.map(larRows => tsRows ++ larRows)
  }

  private def fieldNamesForEdit(edit: ValidationError): Seq[String] = {
    EditMetaDataLookup.forEdit(edit.ruleName).fieldNames
  }

  private def fieldJsonForRow(fieldNames: Seq[String], row: HmdaFileRow): JsObject = {
    val jsVals = fieldNames.map { fieldName =>
      val fieldValue = if (fieldName == "Metropolitan Statistical Area / Metropolitan Division Name") {
        CbsaLookup.nameFor(row.valueOf("Metropolitan Statistical Area / Metropolitan Division").toString)
      } else row.valueOf(fieldName)

      (fieldName, toJsonVal(fieldValue))
    }
    JsObject(jsVals: _*)
  }

  private def tsRow(errors: Seq[ValidationError], ts: Option[TransmittalSheet], fieldNames: Seq[String]): Seq[EditResultRow] = {
    errors.find(_.ts) match {
      case Some(error) =>
        val fieldJson = fieldJsonForRow(fieldNames, ts.getOrElse(TransmittalSheet()))
        Seq(EditResultRow(RowId(error.publicErrorId), fieldJson))
      case None => Seq()
    }
  }

  private def relevantLars[ec: EC, mat: MAT, as: AS](rowIds: Seq[String], eventSource: Source[Event, NotUsed]): Source[LoanApplicationRegister, NotUsed] = {
    eventSource.map {
      case LarValidated(lar, _) if rowIds.contains(lar.loan.id) => lar
      case _ => LoanApplicationRegister()
    }.filterNot(_.isEmpty)
  }

  private def toJsonVal(value: Any) = {
    value match {
      case i: Int => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
    }
  }

}
