package hmda.api.model.validation

import hmda.validation.engine.ValidationError

case class ValidationResult(
  syntactical: List[ValidationError],
  validity: List[ValidationError]
)
