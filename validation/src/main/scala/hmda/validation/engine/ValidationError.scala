package hmda.validation.engine

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

case class ValidationError(id: String, msg: String, errorType: ValidationErrorType)
case class ValidationErrors(errors: Seq[ValidationError])
case class ValidationErrorsSummary(errors: Seq[String])
object ValidationErrors {
  def empty(): ValidationErrors = ValidationErrors(Nil)
}
