package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.HmdaRawDataEvents.HmdaRawDataEvent
import hmda.model.filing.submission.SubmissionId

object HmdaRawDataCommands {

  sealed trait HmdaRawDataCommand extends Command

  case class AddLine(submissionId: SubmissionId,
                     timestamp: Long,
                     data: String,
                     replyTo: Option[ActorRef[HmdaRawDataEvent]])
      extends HmdaRawDataCommand
}
