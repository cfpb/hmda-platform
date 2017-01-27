package hmda.api.http

import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.HmdaFileRow
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine._
import spray.json.{ JsNumber, JsObject, JsString, JsValue }

trait ValidationErrorConverter {

  def validationErrorsToEditResults(vs: HmdaFileValidationState, tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {
    val allErrorsOfThisType: Seq[ValidationError] = (tsErrors ++ larErrors).filter(_.errorType == validationErrorType)

    val errsByEdit: Map[String, Seq[ValidationError]] = allErrorsOfThisType.groupBy(_.ruleName)

    val editResults: Seq[EditResult] = errsByEdit.map {
      case (editName: String, errs: Seq[ValidationError]) =>
        val description = findEditDescription(editName)
        val rows: Seq[EditResultRow] = errs.map(e => editDetail(e, vs))
        EditResult(editName, description, rows)
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

  val editDescriptions = EditMetaDataLookup.values

  private def editDetail(err: ValidationError, vs: HmdaFileValidationState): EditResultRow = {
    val row = relevantRow(err, vs)

    if (err.ts) EditResultRow(RowId("Transmittal Sheet"), relevantFields(err, row))
    else EditResultRow(RowId(err.errorId), relevantFields(err, row))
  }

  private def rowDetail(err: ValidationError, vs: HmdaFileValidationState): RowEditDetail = {
    val name = err.ruleName
    val row = relevantRow(err, vs)
    val fields = relevantFields(err, row)
    RowEditDetail(name, findEditDescription(name), fields)
  }

  private def findEditDescription(editName: String): String = {
    editDescriptions.find(x => x.editNumber == editName)
      .map(_.editDescription)
      .getOrElse("")
  }

  private def relevantRow(err: ValidationError, vs: HmdaFileValidationState): HmdaFileRow = {
    if (err.ts) vs.ts.get
    else vs.lars.find(lar => lar.loan.id == err.errorId).get
  }

  private def relevantFields(err: ValidationError, row: HmdaFileRow): JsObject = {
    val fieldNames: Seq[String] = editDescriptions.find(e => e.editNumber == err.ruleName)
      .map(_.fieldNames).getOrElse(Seq())

    val jsVals: Seq[(String, JsValue)] = fieldNames.map { fieldName =>
      val fieldValue = row.valueOf(fieldName)
      (fieldName, jsonify(fieldValue))
    }

    JsObject(jsVals: _*)
  }

  private def jsonify(value: Any) = {
    value match {
      case i: Int => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
    }
  }

}
