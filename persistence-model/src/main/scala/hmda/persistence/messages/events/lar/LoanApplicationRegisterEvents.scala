package hmda.persistence.messages.events.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.Event

object LoanApplicationRegisterEvents {
  sealed trait LoanApplicationRegisterEvent extends Event
  case class LarAdded(lar: LoanApplicationRegister) extends LoanApplicationRegisterEvent
}
