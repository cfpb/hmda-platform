package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent
}
import hmda.model.filing.submission.{Submission, SubmissionId}

object SubmissionCommands {
  sealed trait SubmissionCommand extends Command

  case class GetSubmission(replyTo: ActorRef[Option[Submission]])
      extends SubmissionCommand

  case class CreateSubmission(submission: SubmissionId,
                              replyTo: ActorRef[SubmissionCreated])
      extends SubmissionCommand
  case class ModifySubmission(submission: Submission,
                              replyTo: ActorRef[SubmissionEvent])
      extends SubmissionCommand
}
