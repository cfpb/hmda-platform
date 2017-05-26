package hmda.persistence.serialization.validation

import hmda.model.validation._
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.model.serialization.HmdaFileValidatorEvents._

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

  def editsVerifiedToProtobuf(event: EditsVerified): EditsVerifiedMessage = {
    EditsVerifiedMessage(
      editType = validationErrorTypeToProtobuf(event.editType),
      verified = event.verified
    )
  }
  def editsVerifiedFromProtobuf(msg: EditsVerifiedMessage): EditsVerified = {
    EditsVerified(
      editType = validationErrorTypeFromProtobuf(msg.editType),
      verified = msg.verified
    )
  }

  def tsSyntacticalErrorToProtobuf(err: TsSyntacticalError): TsSyntacticalErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    TsSyntacticalErrorMessage(error = Some(errProto))
  }
  def tsSyntacticalErrorFromProtobuf(msg: TsSyntacticalErrorMessage): TsSyntacticalError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    TsSyntacticalError(error = err)
  }

  def tsValidityErrorToProtobuf(err: TsValidityError): TsValidityErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    TsValidityErrorMessage(error = Some(errProto))
  }
  def tsValidityErrorFromProtobuf(msg: TsValidityErrorMessage): TsValidityError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    TsValidityError(error = err)
  }

  def tsQualityErrorToProtobuf(err: TsQualityError): TsQualityErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    TsQualityErrorMessage(error = Some(errProto))
  }
  def tsQualityErrorFromProtobuf(msg: TsQualityErrorMessage): TsQualityError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    TsQualityError(error = err)
  }

  def larSyntacticalErrorToProtobuf(err: LarSyntacticalError): LarSyntacticalErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    LarSyntacticalErrorMessage(error = Some(errProto))
  }
  def larSyntacticalErrorFromProtobuf(msg: LarSyntacticalErrorMessage): LarSyntacticalError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    LarSyntacticalError(error = err)
  }

  def larValidityErrorToProtobuf(err: LarValidityError): LarValidityErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    LarValidityErrorMessage(error = Some(errProto))
  }
  def larValidityErrorFromProtobuf(msg: LarValidityErrorMessage): LarValidityError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    LarValidityError(error = err)
  }

  def larQualityErrorToProtobuf(err: LarQualityError): LarQualityErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    LarQualityErrorMessage(error = Some(errProto))
  }
  def larQualityErrorFromProtobuf(msg: LarQualityErrorMessage): LarQualityError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    LarQualityError(error = err)
  }

  def larMacroErrorToProtobuf(err: LarMacroError): LarMacroErrorMessage = {
    val errProto = validationErrorToProtobuf(err.error)
    LarMacroErrorMessage(error = Some(errProto))
  }
  def larMacroErrorFromProtobuf(msg: LarMacroErrorMessage): LarMacroError = {
    val err = validationErrorFromProtobuf(msg.error.getOrElse(ValidationErrorMessage()))
    LarMacroError(error = err)
  }
}
