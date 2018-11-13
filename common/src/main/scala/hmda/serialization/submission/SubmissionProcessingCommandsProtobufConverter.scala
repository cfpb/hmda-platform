package hmda.serialization.submission

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.persistence.serialization.submission.processing.commands._
import SubmissionProtobufConverter._
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.serialization.validation.ValidationProtobufConverter._
import hmda.persistence.serialization.submission.SubmissionIdMessage
import hmda.persistence.serialization.validation.ValidationErrorMessage

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

  def persisteHmdaRowParsedErrorFromProtobuf(
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

  def startSyntacticalValidityToProtobuf(
      cmd: StartSyntacticalValidity): StartSyntacticalValidityMessage = {
    StartSyntacticalValidityMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )
  }

  def startSyntacticalValidityFromProtobuf(
      msg: StartSyntacticalValidityMessage): StartSyntacticalValidity = {
    StartSyntacticalValidity(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def persistHmdaRowValidatedErrorToProtobuf(
      cmd: PersistHmdaRowValidatedError,
      refResolver: ActorRefResolver): PersistHmdaRowValidatedErrorMessage = {
    PersistHmdaRowValidatedErrorMessage(
      cmd.rowNumber,
      Some(validationErrorToProtobuf(cmd.validationError)),
      cmd.replyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )
  }

  def persistHmdaRowValidatedErrorFromProtobuf(
      msg: PersistHmdaRowValidatedErrorMessage,
      refResolver: ActorRefResolver): PersistHmdaRowValidatedError = {
    PersistHmdaRowValidatedError(
      msg.rowNumber,
      validationErrorFromProtobuf(
        msg.validationError.getOrElse(ValidationErrorMessage())),
      if (msg.replyTo == "") None
      else Some(refResolver.resolveActorRef(msg.replyTo))
    )
  }

  def getHmdaValidationErrorStateToProtobuf(cmd: GetHmdaValidationErrorState,
                                            actorRefResolver: ActorRefResolver)
    : GetHmdaValidationErrorStateMessage = {
    GetHmdaValidationErrorStateMessage(
      submissionIdToProtobuf(cmd.submissionId),
      actorRefResolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getHmdaValidationErrorStateFromProtobuf(
      msg: GetHmdaValidationErrorStateMessage,
      actorRefResolver: ActorRefResolver): GetHmdaValidationErrorState = {
    GetHmdaValidationErrorState(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())),
      actorRefResolver.resolveActorRef(msg.replyTo)
    )
  }

}
