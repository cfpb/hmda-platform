package hmda.model.validation

import slick.collection.heterogeneous.Zero.+

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

sealed trait ValidationErrorEntity
case object TsValidationError extends ValidationErrorEntity
case object LarValidationError extends ValidationErrorEntity

sealed trait ValidationError {
  def uli: String
  def editName: String
  def validationErrorType: ValidationErrorType
  def validationErrorEntity: ValidationErrorEntity
  def fields: Map[String, String]
  def toCsv: String =
    s"$validationErrorEntity,$validationErrorType, $editName, $uli"
  def copyWithFields(fields: Map[String, String]): ValidationError
}

case class SyntacticalValidationError(
    uli: String,
    editName: String,
    validationErrorEntity: ValidationErrorEntity,
    fields: Map[String, String])
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Syntactical
  override def copyWithFields(fields: Map[String, String]): SyntacticalValidationError = this.copy(fields = fields)
}

case class ValidityValidationError(uli: String,
                                   editName: String,
                                   validationErrorEntity: ValidationErrorEntity,
                                   fields: Map[String, String])
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Validity
  override def copyWithFields(fields: Map[String, String]): ValidityValidationError = this.copy(fields = fields)
}

case class QualityValidationError(uli: String,
                                  editName: String,
                                  fields: Map[String, String])
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Quality
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
  override def copyWithFields(fields: Map[String, String]): QualityValidationError = this.copy(fields = fields)
}

case class MacroValidationError(editName: String) extends ValidationError {
  override def uli: String = ""
  override def fields: Map[String, String] = Map()
  override def validationErrorType: ValidationErrorType = Macro
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
  override def copyWithFields(fields: Map[String, String]): MacroValidationError = this.copy()
}
