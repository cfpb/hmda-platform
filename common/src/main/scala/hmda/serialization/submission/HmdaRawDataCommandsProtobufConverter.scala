package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.HmdaRawDataCommands.AddLines
import hmda.persistence.serialization.raw.data.commands.{AddLineMessage, AddLinesMessage}
import hmda.persistence.serialization.submission.SubmissionIdMessage
import hmda.serialization.submission.SubmissionProtobufConverter._
// $COVERAGE-OFF$
object HmdaRawDataCommandsProtobufConverter {

  def addLinesToProtobuf(cmd: AddLines, refResolver: ActorRefResolver): AddLinesMessage =
    AddLinesMessage(
      submissionIdToProtobuf(cmd.submissionId),
      cmd.timestamp,
      cmd.data,
      cmd.maybeReplyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )

  def addLineFromProtobuf(msg: AddLineMessage, refResolver: ActorRefResolver): AddLines =
    AddLines(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.timestamp,
      List(msg.data),
      if (msg.maybeReplyTo == "") None
      else Some(refResolver.resolveActorRef(msg.maybeReplyTo))
    )

  def addLinesFromProtobuf(msg: AddLinesMessage, refResolver: ActorRefResolver): AddLines =
    AddLines(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.timestamp,
      msg.data,
      if (msg.maybeReplyTo == "") None
      else Some(refResolver.resolveActorRef(msg.maybeReplyTo))
    )

}
// $COVERAGE-ON$