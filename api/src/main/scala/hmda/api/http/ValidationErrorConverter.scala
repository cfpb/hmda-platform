package hmda.api.http

import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.{ HmdaFileRow, HmdaRowError }
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine._
import spray.json.{ JsNumber, JsObject, JsString, JsValue }

trait ValidationErrorConverter {

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

    errsByEdit.map {
      case (editName: String, _) =>
        EditInfo(editName, editDescription(editName))
    }.toSeq
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
      val fieldValue = row.valueOf(fieldName)
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
