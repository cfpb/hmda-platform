package hmda.api.http

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.api._
import hmda.api.model._
import hmda.census.model.CbsaLookup
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.{ HmdaFileRow, HmdaRowError }
import hmda.model.validation.{ EmptyValidationError, ValidationError }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import spray.json.{ JsNumber, JsObject, JsString, JsValue }

import scala.concurrent.Future

trait ValidationErrorConverter {

  //// New way
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
    uniqueEdits(editType, editSource).map(list =>
      list.map(name => EditInfo(name, editDescription(name))))
  }

  ///// Old way

  def editsOfType(errType: String, vs: HmdaFileValidationState): Seq[ValidationError] = {
    errType.toLowerCase match {
      case "syntactical" => vs.syntacticalErrors
      case "validity" => vs.validityErrors
      case "quality" => vs.qualityErrors
      case "macro" => vs.larMacro
      case _ => Seq()
    }
  }

  def editInfos(edits: Seq[ValidationError]): Seq[EditInfo] = {
    val errsByEdit: Map[String, Seq[ValidationError]] = edits.groupBy(_.ruleName)

    val info = errsByEdit.map {
      case (editName: String, _) =>
        EditInfo(editName, editDescription(editName))
    }.toSeq

    info.sortBy(_.edit)
  }

  def validationErrorsToCsvResults(vs: HmdaFileValidationState): String = {
    val errors: Seq[ValidationError] = vs.allErrors
    val rows: Seq[String] = errors.map(_.toCsv)
    "editType, editId, loanId\n" + rows.mkString("\n")
  }

  def validationErrorToResultRow(err: ValidationError, vs: HmdaFileValidationState): EditResultRow = {
    EditResultRow(RowId(err.publicErrorId), relevantFields(err, vs))
  }

  //// Helper methods

  private def editDescription(editName: String): String = {
    EditMetaDataLookup.forEdit(editName).editDescription
  }

  private def relevantFields(err: ValidationError, vs: HmdaFileValidationState): JsObject = {
    val fieldNames: Seq[String] = EditMetaDataLookup.forEdit(err.ruleName).fieldNames

    val jsVals: Seq[(String, JsValue)] = fieldNames.map { fieldName =>
      val row = relevantRow(err, vs)
      val fieldValue = if (fieldName == "Metropolitan Statistical Area / Metropolitan Division Name") {
        CbsaLookup.nameFor(row.valueOf("Metropolitan Statistical Area / Metropolitan Division").toString)
      } else {
        row.valueOf(fieldName)
      }
      (fieldName, toJsonVal(fieldValue))
    }

    JsObject(jsVals: _*)
  }

  private def relevantRow(err: ValidationError, vs: HmdaFileValidationState): HmdaFileRow = {
    if (err.ts) vs.ts.getOrElse(HmdaRowError())
    else vs.lars.find(lar => lar.loan.id == err.errorId).getOrElse(HmdaRowError())
  }

  private def toJsonVal(value: Any) = {
    value match {
      case i: Int => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
    }
  }

}
