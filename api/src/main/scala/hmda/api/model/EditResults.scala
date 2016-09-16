package hmda.api.model

import hmda.validation.engine.ValidationErrorType

case class EditResult(edit: String, lars: Seq[LarEditResult])
case class LarEditResult(loanId: String)

case class EditResults(editType: ValidationErrorType, edits: Seq[EditResult])

