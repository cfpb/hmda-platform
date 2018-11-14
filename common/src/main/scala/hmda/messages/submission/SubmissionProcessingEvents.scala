package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.validation.ValidationError

object SubmissionProcessingEvents {
  sealed trait SubmissionProcessingEvent extends Event

  case class HmdaRowParsedError(rowNumber: Int, errorMessages: List[String])
      extends SubmissionProcessingEvent

  case class HmdaRowValidatedError(rowNumber: Int,
                                   validationErrors: List[ValidationError])
      extends SubmissionProcessingEvent

  case class HmdaRowParsedCount(count: Int) extends SubmissionProcessingEvent

}
