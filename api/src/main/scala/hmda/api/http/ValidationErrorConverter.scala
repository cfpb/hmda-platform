package hmda.api.http

import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.{ HmdaFileRow, HmdaRowError }
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine._
import spray.json.{ JsNumber, JsObject, JsString, JsValue }

trait ValidationErrorConverter {

  def validationErrorsToEditResults(vs: HmdaFileValidationState, tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {
    val allErrorsOfThisType: Seq[ValidationError] = (tsErrors ++ larErrors).filter(_.errorType == validationErrorType)

    val errsByEdit: Map[String, Seq[ValidationError]] = allErrorsOfThisType.groupBy(_.ruleName)

    val editResults: Seq[EditResult] = errsByEdit.map {
      case (editName: String, errs: Seq[ValidationError]) =>
        val rows: Seq[EditResultRow] = errs.map(e => editDetail(e, vs))
        EditResult(editName, editDescription(editName), rows)
    }.toSeq

    EditResults(editResults)
  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[MacroValidationError] = errors.filter(_.errorType == Macro).asInstanceOf[Seq[MacroValidationError]]
    MacroResults(macroValidationErrors.map(x => MacroResult(x.ruleName, MacroEditJustificationLookup.updateJustifications(x.ruleName, x.justifications))))
  }

  def validationErrorsToRowResults(vs: HmdaFileValidationState, tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], macroErrors: Seq[ValidationError]): RowResults = {
    val tsEdits: Seq[RowEditDetail] = tsErrors.map(e => rowDetail(e, vs))
    val tsRowResults: Seq[RowResult] =
      if (tsEdits.isEmpty) Seq()
      else Seq(RowResult("Transmittal Sheet", tsEdits))

    val larFailuresByRow: Map[String, Seq[ValidationError]] = larErrors.groupBy(_.errorId)
    val larRowResults: Seq[RowResult] = larFailuresByRow.map {
      case (rowId: String, errors: Seq[ValidationError]) =>
        RowResult(rowId, errors.map(e => rowDetail(e, vs)))
    }.toSeq

    val macroResults = validationErrorsToMacroResults(macroErrors)

    RowResults(tsRowResults ++ larRowResults, macroResults)
  }

  //// Helper methods

  private def editDetail(err: ValidationError, vs: HmdaFileValidationState): EditResultRow = {
    if (err.ts) EditResultRow(RowId("Transmittal Sheet"), relevantFields(err, vs))
    else EditResultRow(RowId(err.errorId), relevantFields(err, vs))
  }

  private def rowDetail(err: ValidationError, vs: HmdaFileValidationState): RowEditDetail = {
    val name = err.ruleName
    val fields = relevantFields(err, vs)
    RowEditDetail(name, editDescription(name), fields)
  }

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
