package hmda.validation.engine

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

// errorID = Loan ID (LAR) or Agency Code + Respondent ID (TS)
trait ValidationError {
  def errorId: String
  def ruleName: String
  def errorType: ValidationErrorType
}

case class SyntacticalValidationError(errorId: String, ruleName: String) extends ValidationError {
  override def errorType: ValidationErrorType = Syntactical
}
case class ValidityValidationError(errorId: String, ruleName: String) extends ValidationError {
  override def errorType: ValidationErrorType = Validity
}
case class QualityValidationError(errorId: String, ruleName: String) extends ValidationError {
  override def errorType: ValidationErrorType = Quality
}

case class MacroEditJustification(id: Int = 1, value: String = "", verified: Boolean = false, text: Option[String] = None)

case class MacroValidationError(ruleName: String, justifications: Seq[MacroEditJustification]) extends ValidationError {
  override def errorId: String = ""
  override def errorType: ValidationErrorType = Macro
}

abstract class ValidationErrors {
  def errors: Seq[ValidationError]
}
case class TsValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class LarValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class ValidationErrorsSummary(errors: Seq[String])

