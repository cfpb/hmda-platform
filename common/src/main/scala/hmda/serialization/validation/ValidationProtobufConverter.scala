package hmda.serialization.validation

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.validation._
import hmda.persistence.serialization.submission.processing.events.HmdaRowValidatedErrorMessage
import hmda.persistence.serialization.validation.{ ValidationErrorEntityMessage, ValidationErrorMessage, ValidationErrorTypeMessage }

import scala.collection.immutable._

object ValidationProtobufConverter {

  def validationErrorToProtobuf(cmd: ValidationError): ValidationErrorMessage =
    ValidationErrorMessage(
      cmd.uli,
      cmd.editName,
      validationErrorTypeToProtobuf(cmd.validationErrorType),
      validationErrorEntityToProtobuf(cmd.validationErrorEntity),
      cmd.fields.unzip._1.toSeq,
      cmd.fields.unzip._2.toSeq
    )

  def validationErrorFromProtobuf(msg: ValidationErrorMessage): ValidationError = {
    val entityErrorFromProtobuf = validationErrorEntityFromProtobuf(msg.validationErrorEntity)
    msg.validationErrorType match {
      case ValidationErrorTypeMessage.SYNTACTICAL =>
        SyntacticalValidationError(msg.uli, msg.editName, entityErrorFromProtobuf, ListMap(msg.fieldsKey.zip(msg.fieldsValue): _*))
      case ValidationErrorTypeMessage.VALIDITY =>
        ValidityValidationError(msg.uli, msg.editName, entityErrorFromProtobuf, ListMap(msg.fieldsKey.zip(msg.fieldsValue): _*))
      case ValidationErrorTypeMessage.QUALITY =>
        QualityValidationError(msg.uli, msg.editName, ListMap(msg.fieldsKey.zip(msg.fieldsValue): _*))
      case ValidationErrorTypeMessage.MACRO =>
        MacroValidationError(msg.editName)
      case ValidationErrorTypeMessage.Unrecognized(value) =>
        throw new Exception("Cannot convert from protobuf")
    }
  }

  def validationErrorEntityToProtobuf(cmd: ValidationErrorEntity): ValidationErrorEntityMessage =
    cmd match {
      case TsValidationError  => ValidationErrorEntityMessage.TSVALIDATION
      case LarValidationError => ValidationErrorEntityMessage.LARVALIDATION
    }

  def validationErrorEntityFromProtobuf(msg: ValidationErrorEntityMessage): ValidationErrorEntity =
    msg match {
      case ValidationErrorEntityMessage.TSVALIDATION  => TsValidationError
      case ValidationErrorEntityMessage.LARVALIDATION => LarValidationError
      case ValidationErrorEntityMessage.Unrecognized(value) =>
        throw new Exception("Cannot convert from protobuf")
    }

  def validationErrorTypeToProtobuf(cmd: ValidationErrorType): ValidationErrorTypeMessage =
    cmd match {
      case Syntactical => ValidationErrorTypeMessage.SYNTACTICAL
      case Validity    => ValidationErrorTypeMessage.VALIDITY
      case Quality     => ValidationErrorTypeMessage.QUALITY
      case Macro       => ValidationErrorTypeMessage.MACRO
    }

  def validationErrorTypeFromProtobuf(msg: ValidationErrorTypeMessage): ValidationErrorType =
    msg match {
      case ValidationErrorTypeMessage.SYNTACTICAL => Syntactical
      case ValidationErrorTypeMessage.VALIDITY    => Validity
      case ValidationErrorTypeMessage.QUALITY     => Quality
      case ValidationErrorTypeMessage.MACRO       => Macro
      case ValidationErrorTypeMessage.Unrecognized(value) =>
        throw new Exception("Cannot convert from protobuf")
    }

  def hmdaRowValidatedErrorToProtobuf(cmd: HmdaRowValidatedError): HmdaRowValidatedErrorMessage =
    HmdaRowValidatedErrorMessage(
      cmd.rowNumber,
      cmd.validationErrors.map(e => validationErrorToProtobuf(e))
    )

  def hmdaRowValidatedErrorFromProtobuf(msg: HmdaRowValidatedErrorMessage): HmdaRowValidatedError =
    HmdaRowValidatedError(
      msg.rowNumber,
      msg.validationErrors.map(e => validationErrorFromProtobuf(e)).toList
    )

}
