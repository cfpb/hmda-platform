package hmda.model.validation

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

sealed trait ValidationErrorEntity
case object TsValidationError extends ValidationErrorEntity
case object LarValidationError extends ValidationErrorEntity

trait ValidationError {
  def uli: String
  def editName: String
  def validationErrorType: ValidationErrorType
  def validationErrorEntity: ValidationErrorEntity
  def toCsv: String =
    s"$validationErrorEntity,$validationErrorType, $editName, $uli"
}

case class SyntacticalValidationError(
    uli: String,
    editName: String,
    validationErrorEntity: ValidationErrorEntity)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Syntactical
}

case class ValidityValidationError(uli: String,
                                   editName: String,
                                   validationErrorEntity: ValidationErrorEntity)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Validity
}

case class QualityValidationError(uli: String, editName: String)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Quality
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
}

case class MacroValidationError(editName: String) extends ValidationError {
  override def uli: String = ""
  override def validationErrorType: ValidationErrorType = Macro
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
}
