package hmda.persistence.messages.events.processing

import hmda.model.validation.{ ValidationError, ValidationErrorType }
import hmda.persistence.messages.CommonMessages.Event

object HmdaFileValidatorEvents {
  trait HmdaFileValidatorEvent extends Event

  case class TsSyntacticalError(error: ValidationError) extends HmdaFileValidatorEvent
  case class TsValidityError(error: ValidationError) extends HmdaFileValidatorEvent
  case class TsQualityError(error: ValidationError) extends HmdaFileValidatorEvent
  case class LarSyntacticalError(error: ValidationError) extends HmdaFileValidatorEvent
  case class LarValidityError(error: ValidationError) extends HmdaFileValidatorEvent
  case class LarQualityError(error: ValidationError) extends HmdaFileValidatorEvent
  case class LarMacroError(error: ValidationError) extends HmdaFileValidatorEvent
  case class EditsVerified(editType: ValidationErrorType, verified: Boolean) extends HmdaFileValidatorEvent
}
