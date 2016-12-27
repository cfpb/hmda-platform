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
          if (e.ts) LarEditResult(LarId("Transmittal Sheet"))
          else LarEditResult(LarId(e.errorId))
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

}
