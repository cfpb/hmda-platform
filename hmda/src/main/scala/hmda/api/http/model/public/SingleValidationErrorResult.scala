package hmda.api.http.model.public

case class SingleValidationErrorResult(
    syntactical: ValidationErrorSummary,
    validity: ValidationErrorSummary,
    quality: ValidationErrorSummary
)
