package hmda.persistence.messages.events.validation

import hmda.persistence.messages.CommonMessages.Event

object SubmissionLarStatsEvents {
  trait SubmissionLarStatsEvent extends Event
  case class SubmittedLarsUpdated(totalSubmitted: Int) extends SubmissionLarStatsEvent
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
  ) extends SubmissionLarStatsEvent
}
