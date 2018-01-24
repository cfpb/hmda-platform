package hmda.api.http

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.api._
import hmda.api.model._
import hmda.census.model.CbsaLookup
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.fi.{ HmdaFileRow, HmdaRowError }
import hmda.model.validation.{ EmptyValidationError, ValidationError }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.util.SourceUtils
import spray.json.{ JsNumber, JsObject, JsString, JsValue }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait ValidationErrorConverter extends SourceUtils {

  def editStreamOfType[ec: EC, mat: MAT, as: AS](errType: String, editSource: Source[Event, NotUsed]): Source[ValidationError, NotUsed] = {
    val edits: Source[ValidationError, NotUsed] = errType.toLowerCase match {
      case "syntactical" => editSource.map {
        case LarSyntacticalError(err) => err
        case TsSyntacticalError(err) => err
        case _ => EmptyValidationError
      }
      case "validity" => editSource.map {
        case LarValidityError(err) => err
        case TsValidityError(err) => err
        case _ => EmptyValidationError
      }
      case "quality" => editSource.map {
        case LarQualityError(err) => err
        case TsQualityError(err) => err
        case _ => EmptyValidationError
      }
      case "macro" => editSource.map {
        case LarMacroError(err) => err
        case _ => EmptyValidationError
      }
      case "all" => editSource.map {
        case LarSyntacticalError(err) => err
        case TsSyntacticalError(err) => err
        case LarValidityError(err) => err
        case TsValidityError(err) => err
        case LarQualityError(err) => err
        case TsQualityError(err) => err
        case LarMacroError(err) => err
        case _ => EmptyValidationError
      }
    }

    edits.filter(_ != EmptyValidationError)
  }

  private def uniqueEdits[ec: EC, mat: MAT, as: AS](editType: String, editSource: Source[Event, NotUsed]): Future[List[String]] = {
    var uniqueEdits: List[String] = List()
    val runF = editStreamOfType(editType, editSource).runForeach { e =>
      val name = e.ruleName
      if (!uniqueEdits.contains(name)) uniqueEdits = uniqueEdits :+ name
    }
    runF.map(_ => uniqueEdits)
  }

  def editInfosF[ec: EC, mat: MAT, as: AS](editType: String, editSource: Source[Event, NotUsed]): Future[List[EditInfo]] = {
    uniqueEdits(editType, editSource).map { list =>
      val infos = list.map(name => EditInfo(name, editDescription(name)))
      infos.sortBy(_.edit)
    }
  }

  def editInfosF[ec: EC, mat: MAT, as: AS](editNames: Set[String]): Future[List[EditInfo]] = {
    Future(editNames.toList.sorted.map(name => EditInfo(name, editDescription(name))))
  }

  private val csvHeaderSource = Source.fromIterator(() => Iterator("editType, editId, loanId"))

  def csvResultStream[ec: EC, mat: MAT, as: AS](eventSource: Source[Event, NotUsed]): Source[String, Any] = {
    val edits = editStreamOfType("all", eventSource)
    val csvSource = edits.map(_.toCsv)
    csvHeaderSource.concat(csvSource)
  }

  def validationErrorToResultRow[ec: EC, mat: MAT, as: AS](err: ValidationError, ts: Option[TransmittalSheet], eventSource: Source[Event, NotUsed]): Future[EditResultRow] = {
    relevantFields(err, ts, validatedLars(eventSource)).map { fields =>
      EditResultRow(RowId(err.publicErrorId), fields)
    }
  }

  //// Helper methods

  private def editDescription(editName: String): String = {
    EditMetaDataLookup.forEdit(editName).editDescription
  }

  private def relevantFields[ec: EC, mat: MAT, as: AS](err: ValidationError, ts: Option[TransmittalSheet], lars: Source[LoanApplicationRegister, NotUsed]): Future[JsObject] = {
    val fieldNames: Seq[String] = EditMetaDataLookup.forEdit(err.ruleName).fieldNames

    val jsValsF: Future[Seq[(String, JsValue)]] = Future.sequence {
      fieldNames.map { fieldName =>
        val rowF = relevantRow(err, ts, lars)
        rowF.map { row =>
          val fieldValue = if (fieldName == "Metropolitan Statistical Area / Metropolitan Division Name") {
            CbsaLookup.nameFor(row.valueOf("Metropolitan Statistical Area / Metropolitan Division").toString)
          } else row.valueOf(fieldName)

          (fieldName, toJsonVal(fieldValue))
        }
      }
    }

    jsValsF.map(jsVals => JsObject(jsVals: _*))
  }

  private def relevantRow[ec: EC, mat: MAT, as: AS](err: ValidationError, ts: Option[TransmittalSheet], lars: Source[LoanApplicationRegister, NotUsed]): Future[HmdaFileRow] = {
    if (err.ts) Future(ts.getOrElse(HmdaRowError()))
    else {
      collectHeadValue(lars.filter(lar => lar.loan.id == err.errorId).take(1)).map {
        case Success(lar) => lar
        case Failure(e) => HmdaRowError()
      }
    }
  }

  private def toJsonVal(value: Any) = {
    value match {
      case i: Int => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
    }
  }

  private def validatedLars[ec: EC, mat: MAT, as: AS](eventSource: Source[Event, NotUsed]): Source[LoanApplicationRegister, NotUsed] = {
    eventSource.map {
      case LarValidated(lar, _) => lar
      case _ => LoanApplicationRegister()
    }.filterNot(_.isEmpty)
  }

}
