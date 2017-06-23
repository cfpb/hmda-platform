package hmda.apiModel.model

import hmda.validation.engine.ValidationErrorsSummary

case class SingleValidationErrorResult(
  syntactical: ValidationErrorsSummary,
  validity: ValidationErrorsSummary,
  quality: ValidationErrorsSummary
)
