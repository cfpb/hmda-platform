package hmda.validation.engine

sealed trait ValidationError {
  def id: String
  def msg: String
}
case class SyntacticalError(id: String, msg: String) extends ValidationError
case class ValidityError(id: String, msg: String) extends ValidationError
case class QualityError(id: String, msg: String) extends ValidationError
case class MacroError(id: String, msg: String) extends ValidationError

case class ValidationErrors(errors: Seq[ValidationError])


