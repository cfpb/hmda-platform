package hmda.serialization.filing

import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingStatusUpdated, SubmissionAdded, SubmissionUpdated }
import hmda.persistence.serialization.filing.events.{
  FilingCreatedMessage,
  FilingStatusUpdatedMessage,
  SubmissionAddedMessage,
  SubmissionUpdatedMessage
}
import hmda.persistence.serialization.filing.{ events, FilingMessage }
import hmda.persistence.serialization.submission.SubmissionMessage
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._

object FilingEventsProtobufConverter {

  def filingCreatedToProtobuf(evt: FilingCreated): FilingCreatedMessage =
    FilingCreatedMessage(
      if (evt.filing.isEmpty) None else Some(filingToProtobuf(evt.filing))
    )

  def filingCreatedFromProtobuf(msg: FilingCreatedMessage): FilingCreated =
    FilingCreated(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )

  def filingStatusUpdatedToProtobuf(evt: FilingStatusUpdated): FilingStatusUpdatedMessage =
    events.FilingStatusUpdatedMessage(
      if (evt.filing.isEmpty) None else Some(filingToProtobuf(evt.filing))
    )

  def filingStatusUpdatedFromProtobuf(msg: FilingStatusUpdatedMessage): FilingStatusUpdated =
    FilingStatusUpdated(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )

  def submissionAddedToProtobuf(evt: SubmissionAdded): SubmissionAddedMessage =
    SubmissionAddedMessage(
      if (evt.submission.isEmpty) None
      else Some(submissionToProtobuf(evt.submission))
    )

  def submissionAddedFromProtobuf(msg: SubmissionAddedMessage): SubmissionAdded =
    SubmissionAdded(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )

  def submissionUpdatedToProtobuf(
    evt: SubmissionUpdated
  ): SubmissionUpdatedMessage =
    SubmissionUpdatedMessage(
      if (evt.submission.isEmpty) None
      else Some(submissionToProtobuf(evt.submission))
    )

  def submissionUpdatedFromProtoubf(
    msg: SubmissionUpdatedMessage
  ): SubmissionUpdated =
    SubmissionUpdated(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )

}
