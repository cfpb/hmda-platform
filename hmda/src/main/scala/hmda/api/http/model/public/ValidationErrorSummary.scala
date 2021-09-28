package hmda.api.http.model.public

case class ValidationSingleErrorSummary(edit: String, description: String)
case class ValidationErrorSummary(errors: Seq[ValidationSingleErrorSummary])