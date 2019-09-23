package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.HmdaRawDataCommands.AddLine
import hmda.persistence.serialization.raw.data.commands.AddLineMessage
import hmda.persistence.serialization.submission.SubmissionIdMessage
import hmda.serialization.submission.SubmissionProtobufConverter._

object HmdaRawDataCommandsProtobufConverter {

  def addLineToProtobuf(cmd: AddLine, refResolver: ActorRefResolver): AddLineMessage =
    AddLineMessage(
      submissionIdToProtobuf(cmd.submissionId),
      cmd.timestamp,
      cmd.data,
      cmd.maybeReplyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )

  def addLineFromProtobuf(msg: AddLineMessage, refResolver: ActorRefResolver): AddLine =
    AddLine(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.timestamp,
      msg.data,
      if (msg.maybeReplyTo == "") None
      else Some(refResolver.resolveActorRef(msg.maybeReplyTo))
    )

}
