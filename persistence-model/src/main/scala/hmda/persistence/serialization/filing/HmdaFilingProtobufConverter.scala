package hmda.persistence.serialization.filing

import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.serialization.HmdaFilingEvents.LarValidatedMessage
import hmda.persistence.model.serialization.LoanApplicationRegister.LoanApplicationRegisterMessage
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.serialization.lar.LARProtobufConverter._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object HmdaFilingProtobufConverter {

  def larValidatedToProtobuf(obj: LarValidated): LarValidatedMessage = {
    LarValidatedMessage(
      lar = Some(loanApplicationRegisterToProtobuf(obj.lar)),
      submissionId = Some(submissionIdToProtobuf(obj.submissionId))
    )
  }

  def larValidatedFromProtobuf(msg: LarValidatedMessage): LarValidated = {
    LarValidated(
      lar = loanApplicationRegisterFromProtobuf(msg.lar.getOrElse(LoanApplicationRegisterMessage())),
      submissionId = submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

}
