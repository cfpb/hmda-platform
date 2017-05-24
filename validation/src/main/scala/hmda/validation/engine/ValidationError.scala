package hmda.validation.engine

import hmda.model.validation._

case class SyntacticalValidationError(errorId: String, ruleName: String, ts: Boolean) extends ValidationError {
  override def errorType: ValidationErrorType = Syntactical
}
case class ValidityValidationError(errorId: String, ruleName: String, ts: Boolean) extends ValidationError {
  override def errorType: ValidationErrorType = Validity
}
case class QualityValidationError(errorId: String, ruleName: String, ts: Boolean) extends ValidationError {
  override def errorType: ValidationErrorType = Quality
}

case class MacroValidationError(ruleName: String) extends ValidationError {
  override def ts: Boolean = false
  override def errorId: String = ""
  override def errorType: ValidationErrorType = Macro
}

abstract class ValidationErrors {
  def errors: Seq[ValidationError]
}
case class TsValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class LarValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class ValidationErrorsSummary(errors: Seq[String])

