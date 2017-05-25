package hmda.persistence.messages.events.processing

import hmda.model.validation.{ ValidationError, ValidationErrorType }
import hmda.persistence.messages.CommonMessages.Event

object HmdaFileValidatorEvents {
  trait HmdaFileValidatorEvent extends Event

  case class TsSyntacticalError(error: ValidationError) extends Event
  case class TsValidityError(error: ValidationError) extends Event
  case class TsQualityError(error: ValidationError) extends Event
  case class LarSyntacticalError(error: ValidationError) extends Event
  case class LarValidityError(error: ValidationError) extends Event
  case class LarQualityError(error: ValidationError) extends Event
  case class LarMacroError(error: ValidationError) extends Event
  case class EditsVerified(editType: ValidationErrorType, verified: Boolean) extends Event
}
