package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.SubmissionCommands.{
  CreateSubmission,
  GetSubmission,
  ModifySubmission
}
import hmda.persistence.serialization.submission.commands.{
  CreateSubmissionMessage,
  GetSubmissionMessage,
  ModifySubmissionMessage
}
import SubmissionProtobufConverter._
import hmda.persistence.serialization.submission.{
  SubmissionIdMessage,
  SubmissionMessage
}

object SubmissionCommandsProtobufConverter {

  def createSubmissionToProtobuf(
      cmd: CreateSubmission,
      resolver: ActorRefResolver): CreateSubmissionMessage = {
    CreateSubmissionMessage(
      submissionId = submissionIdToProtobuf(cmd.submissionId),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def createSubmissionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver): CreateSubmission = {
    val msg = CreateSubmissionMessage.parseFrom(bytes)
    val submissionId = submissionIdFromProtobuf(
      msg.submissionId.getOrElse(SubmissionIdMessage()))
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    CreateSubmission(submissionId, actorRef)
  }

  def modifySubmissionToProtobuf(
      cmd: ModifySubmission,
      resolver: ActorRefResolver): ModifySubmissionMessage = {
    ModifySubmissionMessage(
      submission = Some(submissionToProtobuf(cmd.submission)),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def modifySubmisstionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver): ModifySubmission = {
    val msg = ModifySubmissionMessage.parseFrom(bytes)
    val submission = submissionFromProtobuf(
      msg.submission.getOrElse(SubmissionMessage()))
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    ModifySubmission(submission, actorRef)
  }

  def getSubmissionToProtobuf(
      cmd: GetSubmission,
      resolver: ActorRefResolver): GetSubmissionMessage = {
    GetSubmissionMessage(
      resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getSubmissionFromProtobuf(bytes: Array[Byte],
                                resolver: ActorRefResolver): GetSubmission = {
    val msg = GetSubmissionMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    GetSubmission(actorRef)
  }

}
