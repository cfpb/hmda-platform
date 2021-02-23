package hmda.serialization.filing

import akka.actor.typed.ActorRefResolver
import hmda.messages.filing.FilingCommands._
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.filing.commands._
import hmda.persistence.serialization.submission.{ SubmissionIdMessage, SubmissionMessage }
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._
// $COVERAGE-OFF$
object FilingCommandsProtobufConverter {

  def createFilingToProtobuf(cmd: CreateFiling, refResolver: ActorRefResolver): CreateFilingMessage = {
    val filing = cmd.filing
    CreateFilingMessage(
      if (filing.isEmpty) None else Some(filingToProtobuf(filing)),
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def createFilingFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): CreateFiling = {
    val msg      = CreateFilingMessage.parseFrom(bytes)
    val actorRef = refResolver.resolveActorRef(msg.replyTo)
    CreateFiling(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage())),
      actorRef
    )
  }

  def updateFilingStatusToProtobuf(cmd: UpdateFilingStatus, refResolver: ActorRefResolver): UpdateFilingStatusMessage =
    UpdateFilingStatusMessage(
      cmd.period,
      cmd.status.code,
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def updateFilingStatusFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): UpdateFilingStatus = {
    val msg = UpdateFilingStatusMessage.parseFrom(bytes)
    UpdateFilingStatus(
      period = msg.period,
      status = filingStatusFromInt(msg.status),
      replyTo = refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getFilingToProtobuf(cmd: GetFiling, refResolver: ActorRefResolver): GetFilingMessage =
    GetFilingMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getFilingFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetFiling = {
    val msg = GetFilingMessage.parseFrom(bytes)
    GetFiling(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getFilingDetailsToProtobuf(cmd: GetFilingDetails, refResolver: ActorRefResolver): GetFilingDetailsMessage =
    GetFilingDetailsMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getFilingDetailsFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetFilingDetails = {
    val msg = GetFilingDetailsMessage.parseFrom(bytes)
    GetFilingDetails(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def addSubmissionToProtobuf(cmd: AddSubmission, refResolver: ActorRefResolver): AddSubmissionMessage = {
    val submission = cmd.submission
    AddSubmissionMessage(
      if (submission.isEmpty) None
      else Some(submissionToProtobuf(cmd.submission)),
      cmd.replyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )
  }

  def addSubmissionFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): AddSubmission = {
    val msg = AddSubmissionMessage.parseFrom(bytes)
    AddSubmission(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage())),
      if (msg.replyTo == "") None
      else Some(refResolver.resolveActorRef(msg.replyTo))
    )
  }

  def updateSubmissionToProtobuf(
    cmd: UpdateSubmission,
    refResolver: ActorRefResolver
  ): UpdateSubmissionMessage = {
    val submission = cmd.submission
    UpdateSubmissionMessage(
      Some(submissionToProtobuf(submission)),
      cmd.replyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )
  }

  def updateSubmissionFromProtobuf(
    bytes: Array[Byte],
    refResolver: ActorRefResolver
  ): UpdateSubmission = {
    val msg = UpdateSubmissionMessage.parseFrom(bytes)
    UpdateSubmission(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage())),
      if (msg.replyTo == "") None
      else Some(refResolver.resolveActorRef(msg.replyTo))
    )
  }

  def getSubmissionSummaryToProtobuf(cmd: GetSubmissionSummary, refResolver: ActorRefResolver): GetSubmissionSummaryMessage = {
    val submissionId = cmd.submissionId
    GetSubmissionSummaryMessage(
      submissionIdToProtobuf(submissionId),
      refResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getSubmissionSummaryFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetSubmissionSummary = {
    val msg = GetSubmissionSummaryMessage.parseFrom(bytes)
    GetSubmissionSummary(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getLatestSubmissionToProtobuf(cmd: GetLatestSubmission, refResolver: ActorRefResolver): GetLatestSubmissionMessage =
    GetLatestSubmissionMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getLatestSubmissionFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetLatestSubmission = {
    val msg = GetLatestSubmissionMessage.parseFrom(bytes)
    GetLatestSubmission(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getLatestSignedSubmissionToProtobuf(cmd: GetLatestSignedSubmission, refResolver: ActorRefResolver): GetLatestSignedSubmissionMessage =
    GetLatestSignedSubmissionMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getLatestSignedSubmissionFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetLatestSignedSubmission = {
    val msg = GetLatestSignedSubmissionMessage.parseFrom(bytes)
    GetLatestSignedSubmission(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getOldestSignedSubmissionToProtobuf(cmd: GetOldestSignedSubmission, refResolver: ActorRefResolver): GetOldestSignedSubmissionMessage =
    GetOldestSignedSubmissionMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getOldestSignedSubmissionFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetOldestSignedSubmission = {
    val msg = GetOldestSignedSubmissionMessage.parseFrom(bytes)
    GetOldestSignedSubmission(
      refResolver.resolveActorRef(msg.replyTo)
    )
  }

  def getSubmissionsToProtobuf(cmd: GetSubmissions, refResolver: ActorRefResolver): GetSubmissionsMessage =
    GetSubmissionsMessage(
      refResolver.toSerializationFormat(cmd.replyTo)
    )

  def getSubmissionsFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): GetSubmissions = {
    val msg      = GetSubmissionsMessage.parseFrom(bytes)
    val actorRef = refResolver.resolveActorRef(msg.replyTo)
    GetSubmissions(actorRef)
  }

  def filingStopToProtobuf(): FilingStopMessage =
    FilingStopMessage()

  def filingStopFromProtobuf(): FilingStop.type =
    FilingStop

}
// $COVERAGE-ON$