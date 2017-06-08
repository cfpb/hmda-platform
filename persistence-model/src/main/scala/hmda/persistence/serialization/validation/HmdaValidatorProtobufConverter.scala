package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ LarValidated, TsValidated }
import hmda.persistence.model.serialization.CommonHmdaValidator.{ LarValidatedMessage, TsValidatedMessage }
import hmda.persistence.model.serialization.LoanApplicationRegister.LoanApplicationRegisterMessage
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.model.serialization.TransmittalSheet.TransmittalSheetMessage
import hmda.persistence.serialization.lar.LARProtobufConverter._
import hmda.persistence.serialization.ts.TsProtobufConverter._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object HmdaValidatorProtobufConverter {

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

  def tsValidatedToProtobuf(obj: TsValidated): TsValidatedMessage = {
    TsValidatedMessage(
      ts = Some(tsToProtobuf(obj.ts))
    )
  }

  def tsValidatedFromProtobuf(msg: TsValidatedMessage): TsValidated = {
    TsValidated(
      ts = tsFromProtobuf(msg.ts.getOrElse(TransmittalSheetMessage()))
    )
  }
}
