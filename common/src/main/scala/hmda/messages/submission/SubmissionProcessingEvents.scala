package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.filing.HmdaFileRow

object SubmissionProcessingEvents {
  sealed trait SubmissionProcessingEvent extends Event
  case class HmdaRowParsed(hmdaFileRow: HmdaFileRow)
      extends SubmissionProcessingEvent
  case class HmdaRowParsedError(rowNumber: Int, errors: List[String])
      extends SubmissionProcessingEvent
}
