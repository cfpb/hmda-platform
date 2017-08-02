package hmda.persistence.messages.events.processing

import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.persistence.messages.CommonMessages.Event

object CommonHmdaValidatorEvents {
  trait HmdaValidatorEvent extends Event
  case class TsValidated(ts: TransmittalSheet) extends HmdaValidatorEvent
  case class LarValidated(lar: LoanApplicationRegister, submissionId: SubmissionId) extends HmdaValidatorEvent
}
