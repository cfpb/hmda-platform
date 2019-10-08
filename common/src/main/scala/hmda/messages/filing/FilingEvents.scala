package hmda.messages.filing

import hmda.messages.CommonMessages.Event
import hmda.model.filing.Filing
import hmda.model.filing.submission.Submission

object FilingEvents {
  sealed trait FilingEvent                                   extends Event
  final case class FilingCreated(filing: Filing)             extends FilingEvent
  final case class FilingStatusUpdated(filing: Filing)       extends FilingEvent
  final case class SubmissionAdded(submission: Submission)   extends FilingEvent
  final case class SubmissionUpdated(submission: Submission) extends FilingEvent
}
