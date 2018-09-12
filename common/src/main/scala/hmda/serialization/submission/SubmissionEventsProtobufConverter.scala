package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionModified,
  SubmissionNotExists
}
import hmda.persistence.serialization.submission.events.{
  SubmissionCreatedMessage,
  SubmissionModifiedMessage,
  SubmissionNotExistsMessage
}
import SubmissionProtobufConverter._
import hmda.persistence.serialization.submission.{
  SubmissionIdMessage,
  SubmissionMessage
}

object SubmissionEventsProtobufConverter {

  def submissionCreatedToProtobuf(
      evt: SubmissionCreated): SubmissionCreatedMessage = {
    SubmissionCreatedMessage(
      submission = Some(submissionToProtobuf(evt.submission))
    )
  }

  def submissionCreatedFromProtobuf(
      msg: SubmissionCreatedMessage): SubmissionCreated = {
    SubmissionCreated(
      submission =
        submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )
  }

  def submissionModifiedToProtobuf(
      evt: SubmissionModified): SubmissionModifiedMessage = {
    SubmissionModifiedMessage(
      submission = Some(submissionToProtobuf(evt.submission))
    )
  }

  def submissionModifiedFromProtobuf(
      msg: SubmissionModifiedMessage): SubmissionModified = {
    SubmissionModified(
      submission =
        submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )
  }

  def submissionNotExistsToProtobuf(
      evt: SubmissionNotExists): SubmissionNotExistsMessage = {
    SubmissionNotExistsMessage(
      submissionId = submissionIdToProtobuf(evt.submissionId)
    )
  }

  def submissionNotExistsFromProtobuf(
      msg: SubmissionNotExistsMessage): SubmissionNotExists = {
    SubmissionNotExists(
      submissionId = submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())))
  }

}
