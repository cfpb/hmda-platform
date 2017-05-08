package hmda.persistence.serialization.submission

import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus }
import hmda.persistence.messages.events.institutions.SubmissionEvents.{ SubmissionCreated, SubmissionStatusUpdated }
import hmda.persistence.model.serialization.SubmissionEvents._

object SubmissionProtobufConverter {

  def submissionCreatedToProtobuf(obj: SubmissionCreated): SubmissionCreatedMessage = ???

  def submissionCreatedFromProtobuf(msg: SubmissionCreatedMessage): SubmissionCreated = ???

  def submissionStatusUpdatedToProtobuf(obj: SubmissionStatusUpdated): SubmissionStatusUpdatedMessage = ???

  def submissionStatusUpdatedFromProtobuf(msg: SubmissionStatusUpdatedMessage): SubmissionStatusUpdated = ???

  def submissionToProtobuf(obj: Submission): SubmissionMessage = ???

  def submissionFromProtobuf(msg: SubmissionMessage): Submission = ???

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

  def submissionStatusToProtobuf(obj: SubmissionStatus): SubmissionStatusMessage = ???

  def submissionStatusFromProtobuf(msg: SubmissionStatusMessage): SubmissionStatus = ???

}
