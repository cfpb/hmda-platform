package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.HmdaRawDataEvents.HmdaRawDataEvent
import hmda.model.filing.submission.SubmissionId

object HmdaRawDataCommands {

  sealed trait HmdaRawDataCommand extends Command

  case class AddLines(submissionId: SubmissionId, timestamp: Long, data: Seq[String], maybeReplyTo: Option[ActorRef[HmdaRawDataEvent]])
    extends HmdaRawDataCommand

  case object StopRawData extends HmdaRawDataCommand
}