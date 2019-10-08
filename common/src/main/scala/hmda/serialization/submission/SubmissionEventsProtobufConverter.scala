package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionModified, SubmissionNotExists }
import hmda.persistence.serialization.submission.events.{ SubmissionCreatedMessage, SubmissionModifiedMessage, SubmissionNotExistsMessage }
import hmda.persistence.serialization.submission.{ SubmissionIdMessage, SubmissionMessage }
import hmda.serialization.submission.SubmissionProtobufConverter._

object SubmissionEventsProtobufConverter {

  def submissionCreatedToProtobuf(evt: SubmissionCreated): SubmissionCreatedMessage =
    SubmissionCreatedMessage(
      if (evt.submission.isEmpty) None
      else Some(submissionToProtobuf(evt.submission))
    )

  def submissionCreatedFromProtobuf(msg: SubmissionCreatedMessage): SubmissionCreated =
    SubmissionCreated(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )

  def submissionModifiedToProtobuf(evt: SubmissionModified): SubmissionModifiedMessage =
    SubmissionModifiedMessage(
      if (evt.submission.isEmpty) None
      else Some(submissionToProtobuf(evt.submission))
    )

  def submissionModifiedFromProtobuf(msg: SubmissionModifiedMessage): SubmissionModified =
    SubmissionModified(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )

  def submissionNotExistsToProtobuf(evt: SubmissionNotExists): SubmissionNotExistsMessage =
    SubmissionNotExistsMessage(
      submissionIdToProtobuf(evt.submissionId)
    )

  def submissionNotExistsFromProtobuf(msg: SubmissionNotExistsMessage): SubmissionNotExists =
    SubmissionNotExists(submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())))

}
