package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.persistence.serialization.submission.processing.commands._
import SubmissionProtobufConverter._
import hmda.persistence.serialization.submission.SubmissionIdMessage

object SubmissionProcessingCommandsProtobufConverter {

  def startUploadToProtobuf(cmd: StartUpload): StartUploadMessage = {
    StartUploadMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def startUploadFromProtobuf(msg: StartUploadMessage): StartUpload = {
    StartUpload(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def completeUploadToProtobuf(cmd: CompleteUpload): CompleteUploadMessage = {
    CompleteUploadMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def completeUploadFromProtobuf(msg: CompleteUploadMessage): CompleteUpload = {
    CompleteUpload(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def startParsingToProtobuf(cmd: StartParsing): StartParsingMessage = {
    StartParsingMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def startParsingFromProtobuf(msg: StartParsingMessage): StartParsing = {
    StartParsing(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def persistHmdaRowParsedErrorToProtobuf(
      cmd: PersistHmdaRowParsedError,
      refResolver: ActorRefResolver): PersistHmdaRowParsedErrorMessage = {
    PersistHmdaRowParsedErrorMessage(
      cmd.rowNumber,
      cmd.errors,
      cmd.maybeReplyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )
  }

  def persistHmdaRowParsedErrorFromProtobuf(
      msg: PersistHmdaRowParsedErrorMessage,
      refResolver: ActorRefResolver): PersistHmdaRowParsedError = {
    PersistHmdaRowParsedError(
      msg.rowNumber,
      msg.errors.toList,
      if (msg.maybeReplyTo == "") None
      else Some(refResolver.resolveActorRef(msg.maybeReplyTo))
    )
  }

  def getParsedWithErrorCountToProtobuf(
      cmd: GetParsedWithErrorCount,
      resolver: ActorRefResolver): GetParsedWithErrorCountMessage = {
    GetParsedWithErrorCountMessage(
      resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getParsedWithErrorCountFromProtobuf(
      msg: GetParsedWithErrorCountMessage,
      resolver: ActorRefResolver): GetParsedWithErrorCount = {
    GetParsedWithErrorCount(
      resolver.resolveActorRef(msg.replyTo)
    )
  }

  def getParsingErrorsToProtobuf(
      cmd: GetParsingErrors,
      resolver: ActorRefResolver): GetParsingErrorsMessage = {
    GetParsingErrorsMessage(cmd.page,
                            resolver.toSerializationFormat(cmd.replyTo))
  }

  def getParsingErrorsFromProtobuf(
      msg: GetParsingErrorsMessage,
      resolver: ActorRefResolver): GetParsingErrors = {
    GetParsingErrors(msg.page, resolver.resolveActorRef(msg.replyTo))
  }

  def completeParsingToProtobuf(
      cmd: CompleteParsing): CompleteParsingMessage = {
    CompleteParsingMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def completeParsingFromProtobuf(
      msg: CompleteParsingMessage): CompleteParsing = {
    CompleteParsing(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def completeParsingWithErrorsToProtobuf(
      cmd: CompleteParsingWithErrors): CompleteParsingWithErrorsMessage = {
    CompleteParsingWithErrorsMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def completeParsingWithErrorsFromProtobuf(
      msg: CompleteParsingWithErrorsMessage): CompleteParsingWithErrors = {
    CompleteParsingWithErrors(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

}
