package hmda.persistence.serialization.validation

import hmda.model.validation._
import hmda.persistence.model.serialization.HmdaFileValidatorEvents.{ ValidationErrorMessage, ValidationErrorTypeMessage }

object HmdaFileValidatorProtobufConverter {

  def validationErrorTypeToProtobuf(err: ValidationErrorType): ValidationErrorTypeMessage = {
    err match {
      case Syntactical => ValidationErrorTypeMessage.SYNTACTICAL
      case Validity => ValidationErrorTypeMessage.VALIDITY
      case Quality => ValidationErrorTypeMessage.QUALITY
      case Macro => ValidationErrorTypeMessage.MACRO
    }
  }
  def validationErrorTypeFromProtobuf(msg: ValidationErrorTypeMessage): ValidationErrorType = {
    msg.value match {
      case 0 => Syntactical
      case 1 => Validity
      case 2 => Quality
      case 3 => Macro
    }
  }

  def validationErrorToProtobuf(err: ValidationError): ValidationErrorMessage = {
    ValidationErrorMessage(
      errorId = err.errorId,
      ruleName = err.ruleName,
      errorType = validationErrorTypeToProtobuf(err.errorType),
      ts = err.ts
    )
  }
  def validationErrorFromProtobuf(msg: ValidationErrorMessage): ValidationError = {
    val errorType = validationErrorTypeFromProtobuf(msg.errorType)
    val errorId = msg.errorId
    val ruleName = msg.ruleName
    val ts = msg.ts

    errorType match {
      case Syntactical => SyntacticalValidationError(errorId, ruleName, ts)
      case Validity => ValidityValidationError(errorId, ruleName, ts)
      case Quality => QualityValidationError(errorId, ruleName, ts)
      case Macro => MacroValidationError(ruleName)
    }
  }
}
