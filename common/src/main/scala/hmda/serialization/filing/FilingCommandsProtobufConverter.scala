package hmda.serialization.filing

import akka.actor.typed.ActorRefResolver
import hmda.messages.filing.FilingCommands._
import hmda.persistence.serialization.filing.commands._
import FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.submission.SubmissionMessage

object FilingCommandsProtobufConverter {

  def createFilingToProtobuf(
      cmd: CreateFiling,
      refResolver: ActorRefResolver): CreateFilingMessage = {
    val filing = cmd.filing
    CreateFilingMessage(
      if (filing.isEmpty) None else Some(filingToProtobuf(filing)),
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def createFilingFromProtobuf(bytes: Array[Byte],
                               refResolver: ActorRefResolver): CreateFiling = {
    val msg = CreateFilingMessage.parseFrom(bytes)
    val actorRef = refResolver.resolveActorRef(msg.replyTo)
    CreateFiling(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage())),
      actorRef
    )
  }

  def updateFilingStatusToProtobuf(
      cmd: UpdateFilingStatus,
      refResolver: ActorRefResolver): UpdateFilingStatusMessage = {
    UpdateFilingStatusMessage(
      cmd.period,
      cmd.status.code,
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def updateFilingStatusFromProtobuf(
      bytes: Array[Byte],
      refResolver: ActorRefResolver): UpdateFilingStatus = {
    val msg = UpdateFilingStatusMessage.parseFrom(bytes)
    UpdateFilingStatus(
      period = msg.period,
      status = filingStatusFromInt(msg.status),
      replyTo = refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getFilingToProtobuf(cmd: GetFiling,
                          refResolver: ActorRefResolver): GetFilingMessage = {
    GetFilingMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getFilingFromProtobuf(bytes: Array[Byte],
                            refResolver: ActorRefResolver): GetFiling = {
    val msg = GetFilingMessage.parseFrom(bytes)
    GetFiling(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def addSubmissionToProtobuf(
      cmd: AddSubmission,
      refResolver: ActorRefResolver): AddSubmissionMessage = {
    val submission = cmd.submission
    AddSubmissionMessage(
      if (submission.isEmpty) None else Some(submissionToProtobuf(cmd.submission)),
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def addSubmissionFromProtobuf(bytes: Array[Byte],
                                refResolver: ActorRefResolver): AddSubmission = {
    val msg = AddSubmissionMessage.parseFrom(bytes)
    AddSubmission(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage())),
      refResolver.resolveActorRef(msg.replyTo)
    )
  }


  def getLatestSubmissionToProtobuf(
      cmd: GetLatestSubmission,
      refResolver: ActorRefResolver): GetLatestSubmissionMessage = {
    GetLatestSubmissionMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getLatestSubmissionFromProtobuf(
      bytes: Array[Byte],
      refResolver: ActorRefResolver): GetLatestSubmission = {
    val msg = GetLatestSubmissionMessage.parseFrom(bytes)
    GetLatestSubmission(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getSubmissionsToProtobuf(
      cmd: GetSubmissions,
      refResolver: ActorRefResolver): GetSubmissionsMessage = {
    GetSubmissionsMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getSubmissionsFromProtobuf(
      bytes: Array[Byte],
      refResolver: ActorRefResolver): GetSubmissions = {
    val msg = GetSubmissionsMessage.parseFrom(bytes)
    val actorRef = refResolver.resolveActorRef(msg.replyTo)
    GetSubmissions(actorRef)
  }

}
