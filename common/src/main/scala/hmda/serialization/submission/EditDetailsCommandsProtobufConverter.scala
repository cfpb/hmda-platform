package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.EditDetailsCommands.{ GetEditDetails, GetEditRowCount, PersistEditDetails }
import hmda.persistence.serialization.edit.details.EditDetailsMessage
import hmda.persistence.serialization.edit.details.commands.{ GetEditDetailsMessage, GetEditRowCountMessage, PersistEditDetailsMessage }
import hmda.persistence.serialization.submission.SubmissionIdMessage
import hmda.serialization.submission.EditDetailsProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._
// $COVERAGE-OFF$
object EditDetailsCommandsProtobufConverter {

  def persistEditDetailsToProtobuf(cmd: PersistEditDetails, refResolver: ActorRefResolver): PersistEditDetailsMessage =
    PersistEditDetailsMessage(
      Some(editDetailsToProtobuf(cmd.editDetails)),
      cmd.replyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )

  def persistEditDetailsFromProtobuf(msg: PersistEditDetailsMessage, refResolver: ActorRefResolver): PersistEditDetails =
    PersistEditDetails(
      editDetailsFromProtobuf(msg.editDetails.getOrElse(EditDetailsMessage())),
      if (msg.replyTo == "") None
      else Some(refResolver.resolveActorRef(msg.replyTo))
    )

  def getEditRowCountToProtobuf(cmd: GetEditRowCount, refResolver: ActorRefResolver): GetEditRowCountMessage =
    GetEditRowCountMessage(
      cmd.editName,
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getEditRowCountFromProtobuf(msg: GetEditRowCountMessage, refResolver: ActorRefResolver): GetEditRowCount =
    GetEditRowCount(
      msg.editName,
      refResolver.resolveActorRef(msg.replyTo)
    )

  def getEditDetailsToProtobuf(cmd: GetEditDetails, refResolver: ActorRefResolver): GetEditDetailsMessage =
    GetEditDetailsMessage(
      submissionIdToProtobuf(cmd.submissionId),
      cmd.editName,
      cmd.page,
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getEditDetailsFromProtobuf(msg: GetEditDetailsMessage, refResolver: ActorRefResolver): GetEditDetails =
    GetEditDetails(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.editName,
      msg.page,
      refResolver.resolveActorRef(msg.replyTo)
    )

}
// $COVERAGE-ON$
