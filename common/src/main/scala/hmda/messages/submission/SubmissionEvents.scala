package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.filing.submission.Submission

object SubmissionEvents {
  sealed trait SubmissionEvent extends Event
  case class EmptySubmission(submission: Submission = Submission())
      extends SubmissionEvent
  case class SubmissionCreated(submission: Submission) extends SubmissionEvent
  case class SubmissionModified(submission: Submission) extends SubmissionEvent
  case object SubmissionNotExists extends SubmissionEvent
}
