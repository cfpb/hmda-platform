package hmda.validation.engine

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

// errorID = Loan ID (LAR) or Agency Code + Respondent ID (TS)
trait ValidationError {
  def errorId: String
  def name: String
  def errorType: ValidationErrorType
}

case class SyntacticalValidationError(errorId: String, name: String) extends ValidationError {
  override def errorType: ValidationErrorType = Syntactical
}
case class ValidityValidationError(errorId: String, name: String) extends ValidationError {
  override def errorType: ValidationErrorType = Validity
}
case class QualityValidationError(errorId: String, name: String) extends ValidationError {
  override def errorType: ValidationErrorType = Quality
}

case class MacroEditJustification(id: Int, value: String, verified: Boolean, text: Option[String])

case class MacroValidationError(name: String, justifications: Seq[MacroEditJustification]) extends ValidationError {
  override def errorId: String = ""
  override def errorType: ValidationErrorType = Macro
}

//case class ValidationError(errorId: String, name: String, errorType: ValidationErrorType)
abstract class ValidationErrors {
  def errors: Seq[ValidationError]
}
case class TsValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class LarValidationErrors(errors: Seq[ValidationError]) extends ValidationErrors
case class ValidationErrorsSummary(errors: Seq[String])

