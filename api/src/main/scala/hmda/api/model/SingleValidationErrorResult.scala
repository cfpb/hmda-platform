package hmda.api.model

import hmda.validation.engine.ValidationErrors

case class SingleValidationErrorResult(
  syntactical: ValidationErrors,
  validity: ValidationErrors,
  quality: ValidationErrors,
  `macro`: ValidationErrors
)
