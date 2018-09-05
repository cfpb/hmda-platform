package hmda.api.http.model.public

case class SingleValidationErrorResult(
    syntactical: ValidationErrorSummary = ValidationErrorSummary(Nil),
    validity: ValidationErrorSummary = ValidationErrorSummary(Nil),
    quality: ValidationErrorSummary = ValidationErrorSummary(Nil)
)
