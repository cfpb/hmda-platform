package hmda.messages.filing

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.model.filing.{ Filing, FilingDetails, FilingStatus }

object FilingCommands {
  sealed trait FilingCommand                                                      extends Command
  final case class CreateFiling(filing: Filing, replyTo: ActorRef[FilingCreated]) extends FilingCommand

  final case class UpdateFilingStatus(period: String, status: FilingStatus, replyTo: ActorRef[Filing]) extends FilingCommand

  final case class GetFiling(replyTo: ActorRef[Option[Filing]]) extends FilingCommand

  final case class GetFilingDetails(replyTo: ActorRef[Option[FilingDetails]]) extends FilingCommand

  final case class AddSubmission(submission: Submission, replyTo: Option[ActorRef[Submission]]) extends FilingCommand

  final case class GetLatestSubmission(replyTo: ActorRef[Option[Submission]]) extends FilingCommand

  final case class GetLatestSignedSubmission(replyTo: ActorRef[Option[Submission]]) extends FilingCommand

  final case class GetOldestSignedSubmission(replyTo: ActorRef[Option[Submission]]) extends FilingCommand

  final case class GetSubmissionSummary(submissionId: SubmissionId, replyTo: ActorRef[Option[Submission]]) extends FilingCommand

  final case class UpdateSubmission(submission: Submission, replyTo: Option[ActorRef[Submission]]) extends FilingCommand

  final case class GetSubmissions(replyTo: ActorRef[List[Submission]]) extends FilingCommand

  final case class FilingStop() extends FilingCommand
}
