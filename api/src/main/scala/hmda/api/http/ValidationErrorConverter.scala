package hmda.api.http

import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.validation.engine._

trait ValidationErrorConverter {

  val editDescriptions = EditMetaDataLookup.values

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {
    val allErrorsOfThisType: Seq[ValidationError] = (tsErrors ++ larErrors).filter(_.errorType == validationErrorType)

    val errsByEdit: Map[String, Seq[ValidationError]] = allErrorsOfThisType.groupBy(_.ruleName)

    val editResults: Seq[EditResult] = errsByEdit.map {
      case (editName: String, errs: Seq[ValidationError]) =>
        val description = findEditDescription(editName)
        val rowIds = errs.map { e =>
          if (e.ts) EditResultRow(RowId("Transmittal Sheet"))
          else EditResultRow(RowId(e.errorId))
        }
        EditResult(editName, description, rowIds)
    }.toSeq

    EditResults(editResults)
  }

  private def findEditDescription(editName: String): String = {
    editDescriptions.find(x => x.editNumber == editName)
      .map(_.editDescription)
      .getOrElse("")
  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[MacroValidationError] = errors.filter(_.errorType == Macro).asInstanceOf[Seq[MacroValidationError]]
    MacroResults(macroValidationErrors.map(x => MacroResult(x.ruleName, MacroEditJustificationLookup.updateJustifications(x.ruleName, x.justifications))))
  }

  def validationErrorsToRowResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], macroErrors: Seq[ValidationError]): RowResults = {
    val tsEdits: Seq[RowEditDetail] = tsErrors.map(rowDetail)
    val tsRowResults: Seq[RowResult] =
      if (tsEdits.isEmpty) Seq()
      else Seq(RowResult("Transmittal Sheet", tsEdits))

    val larFailuresByRow: Map[String, Seq[ValidationError]] = larErrors.groupBy(_.errorId)
    val larRowResults: Seq[RowResult] = larFailuresByRow.map {
      case (rowId: String, errors: Seq[ValidationError]) => RowResult(rowId, errors.map(rowDetail))
    }.toSeq

    val macroResults = validationErrorsToMacroResults(macroErrors)

    RowResults(tsRowResults ++ larRowResults, macroResults)
  }

  private def rowDetail(err: ValidationError): RowEditDetail = {
    val name = err.ruleName
    RowEditDetail(name, findEditDescription(name))
  }

}
