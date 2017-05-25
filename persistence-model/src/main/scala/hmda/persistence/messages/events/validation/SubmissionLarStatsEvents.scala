package hmda.persistence.messages.events.validation

import hmda.persistence.messages.CommonMessages.Event

object SubmissionLarStatsEvents {
  case class SubmittedLarsUpdated(totalSubmitted: Int) extends Event
  case class MacroStatsUpdated(
    totalValidated: Int,
    q070Total: Int,
    q070Sold: Int,
    q071Total: Int,
    q071Sold: Int,
    q072Total: Int,
    q072Sold: Int,
    q075Ratio: Double,
    q076Ratio: Double
  ) extends Event
}
