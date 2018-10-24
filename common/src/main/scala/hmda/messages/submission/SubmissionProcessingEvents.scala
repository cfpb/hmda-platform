package hmda.messages.submission

import hmda.messages.CommonMessages.Event

object SubmissionProcessingEvents {
  sealed trait SubmissionProcessingEvent extends Event
  case class HmdaRowParsed() extends SubmissionProcessingEvent

}
