package hmda.persistence.messages.events.validation

import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.Event

object ValidationStatsEvents {
  trait ValidationStatsEvent extends Event

  case class SubmissionSubmittedTotalsAdded(total: Int, id: SubmissionId) extends ValidationStatsEvent
  case class SubmissionTaxIdAdded(taxId: String, id: SubmissionId) extends ValidationStatsEvent
  case class SubmissionMacroStatsAdded(
    id: SubmissionId,
    total: Int,
    q070: Int,
    q070Sold: Int,
    q071Lars: Int,
    q071Sold: Int,
    q072Lars: Int,
    q072Sold: Int,
    q075Ratio: Double,
    q076Ratio: Double
  ) extends ValidationStatsEvent

}
