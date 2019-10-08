package hmda.messages.submission

import hmda.messages.CommonMessages.Command
import hmda.messages.submission.SubmissionEvents.SubmissionEvent
import hmda.model.filing.submission.Submission

object SubmissionManagerCommands {

  sealed trait SubmissionManagerCommand extends Command

  case class UpdateSubmissionStatus(submission: Submission) extends SubmissionManagerCommand

  case object SubmissionManagerStop extends SubmissionManagerCommand

  case class WrappedSubmissionEventResponse(submissionEvent: SubmissionEvent) extends SubmissionManagerCommand

}
