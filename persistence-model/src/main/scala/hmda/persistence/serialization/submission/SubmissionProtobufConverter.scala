package hmda.persistence.serialization.submission

import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus, Created, Uploading, Uploaded, Parsing, Parsed, ParsedWithErrors, Validating, ValidatedWithErrors, Validated, Signed, Failed }
import hmda.persistence.messages.events.institutions.SubmissionEvents.{ SubmissionCreated, SubmissionStatusUpdated }
import hmda.persistence.model.serialization.SubmissionEvents._

object SubmissionProtobufConverter {

  def submissionCreatedToProtobuf(obj: SubmissionCreated): SubmissionCreatedMessage = ???

  def submissionCreatedFromProtobuf(msg: SubmissionCreatedMessage): SubmissionCreated = ???

  def submissionStatusUpdatedToProtobuf(obj: SubmissionStatusUpdated): SubmissionStatusUpdatedMessage = ???

  def submissionStatusUpdatedFromProtobuf(msg: SubmissionStatusUpdatedMessage): SubmissionStatusUpdated = ???

  def submissionToProtobuf(obj: Submission): SubmissionMessage = {
    SubmissionMessage(
      id = Some(submissionIdToProtobuf(obj.id)),
      status = Some(submissionStatusToProtobuf(obj.status)),
      start = obj.start,
      end = obj.end,
      receipt = obj.receipt
    )
  }

  def submissionFromProtobuf(msg: SubmissionMessage): Submission = {
    Submission(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      status = submissionStatusFromProtobuf(msg.status.getOrElse(SubmissionStatusMessage())),
      start = msg.start,
      end = msg.end,
      receipt = msg.receipt
    )
  }

  def submissionIdToProtobuf(obj: SubmissionId): SubmissionIdMessage = {
    SubmissionIdMessage(
      institutionId = obj.institutionId,
      period = obj.period,
      sequenceNumber = obj.sequenceNumber
    )
  }

  def submissionIdFromProtobuf(msg: SubmissionIdMessage): SubmissionId = {
    SubmissionId(
      institutionId = msg.institutionId,
      period = msg.period,
      sequenceNumber = msg.sequenceNumber
    )
  }

  def submissionStatusToProtobuf(obj: SubmissionStatus): SubmissionStatusMessage = {
    SubmissionStatusMessage(
      code = obj.code,
      message = obj.message,
      description = obj.description
    )
  }

  def submissionStatusFromProtobuf(msg: SubmissionStatusMessage): SubmissionStatus = {
    msg.code match {
      case 1 => Created
      case 2 => Uploading
      case 3 => Uploaded
      case 4 => Parsing
      case 5 => ParsedWithErrors
      case 6 => Parsed
      case 7 => Validating
      case 8 => ValidatedWithErrors
      case 9 => Validated
      case 10 => Signed
      case -1 => Failed(msg.message)
    }
  }

}
