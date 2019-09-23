package hmda.model.validation

import scala.collection.immutable._

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity    extends ValidationErrorType
case object Quality     extends ValidationErrorType
case object Macro       extends ValidationErrorType

sealed trait ValidationErrorEntity
case object TsValidationError  extends ValidationErrorEntity
case object LarValidationError extends ValidationErrorEntity

sealed trait ValidationError {
  def uli: String
  def editName: String
  def validationErrorType: ValidationErrorType
  def validationErrorEntity: ValidationErrorEntity
  def fields: ListMap[String, String]
  def toCsv: String =
    s"$validationErrorEntity,$validationErrorType, $editName, $uli"
  def copyWithFields(fields: ListMap[String, String]): ValidationError
}

case class SyntacticalValidationError(uli: String,
                                      editName: String,
                                      validationErrorEntity: ValidationErrorEntity,
                                      fields: ListMap[String, String] = ListMap.empty)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Syntactical
  override def copyWithFields(fields: ListMap[String, String]): SyntacticalValidationError =
    this.copy(fields = fields)
}

case class ValidityValidationError(uli: String,
                                   editName: String,
                                   validationErrorEntity: ValidationErrorEntity,
                                   fields: ListMap[String, String] = ListMap.empty)
    extends ValidationError {
  override def validationErrorType: ValidationErrorType = Validity
  override def copyWithFields(fields: ListMap[String, String]): ValidityValidationError =
    this.copy(fields = fields)
}

case class QualityValidationError(uli: String, editName: String, fields: ListMap[String, String] = ListMap.empty) extends ValidationError {
  override def validationErrorType: ValidationErrorType     = Quality
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
  override def copyWithFields(fields: ListMap[String, String]): QualityValidationError =
    this.copy(fields = fields)
}

case class MacroValidationError(editName: String) extends ValidationError {
  override def uli: String                                                           = ""
  override def fields: ListMap[String, String]                                       = ListMap.empty
  override def validationErrorType: ValidationErrorType                              = Macro
  override def validationErrorEntity: ValidationErrorEntity                          = LarValidationError
  override def copyWithFields(fields: ListMap[String, String]): MacroValidationError = this.copy()
}

case class EmptyMacroValidationError() extends ValidationError {
  override def uli: String                                  = ""
  override def editName: String                             = ""
  override def validationErrorType: ValidationErrorType     = Macro
  override def validationErrorEntity: ValidationErrorEntity = LarValidationError
  override def fields: ListMap[String, String]              = ListMap.empty
  override def copyWithFields(fields: ListMap[String, String]): ValidationError =
    this
}
