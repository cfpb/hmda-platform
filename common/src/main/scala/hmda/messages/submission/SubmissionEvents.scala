package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.filing.submission.{ Submission, SubmissionId }

object SubmissionEvents {
  sealed trait SubmissionEvent                                     extends Event
  final case class SubmissionCreated(submission: Submission)       extends SubmissionEvent
  final case class SubmissionModified(submission: Submission)      extends SubmissionEvent
  final case class SubmissionNotExists(submissionId: SubmissionId) extends SubmissionEvent
}
