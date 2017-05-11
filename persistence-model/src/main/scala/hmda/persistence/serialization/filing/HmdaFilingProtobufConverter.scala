package hmda.persistence.serialization.filing

import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.serialization.HmdaFilingEvents.LarValidatedMessage

object HmdaFilingProtobufConverter {

  def larValidatedToProtobuf(obj: LarValidated): LarValidatedMessage = ???

  def larValidatedFromProtobuf(msg: LarValidatedMessage): LarValidated = ???

}
