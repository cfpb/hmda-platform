package hmda.api.model

import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine.{ValidationError, ValidationErrorType}

case class EditResult(edit: String, lars: Seq[LarEditResult])
case class LarEditResult(loanId: String)

case class EditResults(editType: ValidationErrorType, edits: Seq[EditResult])

object EditResults {
  def apply(errors: Seq[ValidationError]): EditResults = {

  }
}

