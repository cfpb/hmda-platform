package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionEvent }
import hmda.model.filing.submission.{ Submission, SubmissionId }

object SubmissionCommands {
  sealed trait SubmissionCommand extends Command

  final case class GetSubmission(replyTo: ActorRef[Option[Submission]]) extends SubmissionCommand

  final case class CreateSubmission(submissionId: SubmissionId, replyTo: ActorRef[SubmissionCreated]) extends SubmissionCommand
  final case class ModifySubmission(submission: Submission, replyTo: ActorRef[SubmissionEvent])       extends SubmissionCommand

  final case class SubmissionStop() extends SubmissionCommand
}
