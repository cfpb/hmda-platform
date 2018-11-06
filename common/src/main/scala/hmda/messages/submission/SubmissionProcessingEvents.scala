package hmda.messages.submission

import hmda.messages.CommonMessages.Event

object SubmissionProcessingEvents {
  sealed trait SubmissionProcessingEvent extends Event

  case class HmdaRowParsedError(rowNumber: Int, errorMessages: List[String])
      extends SubmissionProcessingEvent

  case class HmdaRowParsedCount(count: Int) extends SubmissionProcessingEvent
}
