package hmda.model.validation

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

trait ValidationError {
  def uli: String
  def editName: String
  def validationErrorType: ValidationErrorType
  def toCsv: String = s"$validationErrorType, $editName, $uli"
}

case class SyntacticalValidationError(uli: String, editName: String)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Syntactical
}

case class ValidityValidationError(uli: String, editName: String)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Validity
}

case class QualityValidationError(uli: String, editName: String)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Quality
}

case class MacroValidationError(editName: String) extends ValidationError {
  override def uli: String = ""
  override def validationErrorType: ValidationErrorType = Macro
}
