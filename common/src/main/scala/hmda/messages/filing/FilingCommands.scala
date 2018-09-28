package hmda.messages.filing

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.model.filing.submission.Submission
import hmda.model.filing.{Filing, FilingStatus}

object FilingCommands {
  sealed trait FilingCommand extends Command
  case class CreateFiling(filing: Filing, replyTo: ActorRef[Option[Filing]])
      extends FilingCommand
  case class UpdateFilingStatus(period: String,
                                status: FilingStatus,
                                replyTo: ActorRef[Filing])
      extends FilingCommand
  case class GetFilingByPeriod(period: String,
                               replyTo: ActorRef[Option[Filing]])
      extends FilingCommand
  case class AddSubmission(submission: Submission,
                           replyTo: ActorRef[Submission])
      extends FilingCommand
  case class GetLatestSubmission(replyTo: ActorRef[Option[Submission]])
      extends FilingCommand

  case class GetSubmissions(replyTo: ActorRef[List[Submission]])
      extends FilingCommand
}
