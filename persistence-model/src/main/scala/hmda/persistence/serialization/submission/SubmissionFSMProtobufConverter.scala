package hmda.persistence.serialization.submission

import hmda.persistence.messages.events.processing.SubmissionFSMEvents._
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionMessage
import hmda.persistence.model.serialization.SubmissionFSM._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object SubmissionFSMProtobufConverter {

  def submissionFSMCreatedFromProtobuf(event: SubmissionFSMCreatedMessage): SubmissionFSMCreated = {
    SubmissionFSMCreated(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionFSMCreatedToProtobuf(msg: SubmissionFSMCreated): SubmissionFSMCreatedMessage = {
    SubmissionFSMCreatedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionUploadingFromProtobuf(event: SubmissionUploadingMessage): SubmissionUploading = {
    SubmissionUploading(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionUploadingToProtobuf(msg: SubmissionUploading): SubmissionUploadingMessage = {
    SubmissionUploadingMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionUploadedFromProtobuf(event: SubmissionUploadedMessage): SubmissionUploaded = {
    SubmissionUploaded(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionUploadedToProtobuf(msg: SubmissionUploaded): SubmissionUploadedMessage = {
    SubmissionUploadedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionParsingFromProtobuf(event: SubmissionParsingMessage): SubmissionParsing = {
    SubmissionParsing(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionParsingToProtobuf(msg: SubmissionParsing): SubmissionParsingMessage = {
    SubmissionParsingMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionParsedFromProtobuf(event: SubmissionParsedMessage): SubmissionParsed = {
    SubmissionParsed(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionParsedToProtobuf(msg: SubmissionParsed): SubmissionParsedMessage = {
    SubmissionParsedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionParsedWithErrorsFromProtobuf(event: SubmissionParsedWithErrorsMessage): SubmissionParsedWithErrors = {
    SubmissionParsedWithErrors(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionParsedWithErrorsToProtobuf(msg: SubmissionParsedWithErrors): SubmissionParsedWithErrorsMessage = {
    SubmissionParsedWithErrorsMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionValidatingFromProtobuf(event: SubmissionValidatingMessage): SubmissionValidating = {
    SubmissionValidating(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionValidatingToProtobuf(msg: SubmissionValidating): SubmissionValidatingMessage = {
    SubmissionValidatingMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionValidatedFromProtobuf(event: SubmissionValidatedMessage): SubmissionValidated = {
    SubmissionValidated(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionValidatedToProtobuf(msg: SubmissionValidated): SubmissionValidatedMessage = {
    SubmissionValidatedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionValidatedWithErrorsFromProtobuf(event: SubmissionValidatedWithErrorsMessage): SubmissionValidatedWithErrors = {
    SubmissionValidatedWithErrors(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionValidatedWithErrorsToProtobuf(msg: SubmissionValidatedWithErrors): SubmissionValidatedWithErrorsMessage = {
    SubmissionValidatedWithErrorsMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionSignedFromProtobuf(event: SubmissionSignedMessage): SubmissionSigned = {
    SubmissionSigned(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionSignedToProtobuf(msg: SubmissionSigned): SubmissionSignedMessage = {
    SubmissionSignedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

  def submissionFailedFromProtobuf(event: SubmissionFailedMessage): SubmissionFailed = {
    SubmissionFailed(s = submissionFromProtobuf(event.submission.getOrElse(SubmissionMessage())))
  }
  def submissionFailedToProtobuf(msg: SubmissionFailed): SubmissionFailedMessage = {
    SubmissionFailedMessage(submission = Some(submissionToProtobuf(msg.s)))
  }

}
