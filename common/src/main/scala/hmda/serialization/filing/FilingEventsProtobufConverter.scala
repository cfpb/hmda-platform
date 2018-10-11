package hmda.serialization.filing

import hmda.messages.filing.FilingEvents.{
  FilingCreated,
  FilingStatusUpdated,
  SubmissionAdded
}
import hmda.persistence.serialization.filing.events.{
  FilingCreatedMessage,
  FilingStatusUpdatedMessage,
  SubmissionAddedMessage
}
import FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._
import hmda.persistence.serialization.filing.{FilingMessage, events}
import hmda.persistence.serialization.submission.SubmissionMessage

object FilingEventsProtobufConverter {

  def filingCreatedToProtobuf(evt: FilingCreated): FilingCreatedMessage = {
    FilingCreatedMessage(
      if (evt.filing.isEmpty) None else Some(filingToProtobuf(evt.filing))
    )
  }

  def filingCreatedFromProtobuf(msg: FilingCreatedMessage): FilingCreated = {
    FilingCreated(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )
  }

  def filingStatusUpdatedToProtobuf(
      evt: FilingStatusUpdated): FilingStatusUpdatedMessage = {
    events.FilingStatusUpdatedMessage(
      if (evt.filing.isEmpty) None else Some(filingToProtobuf(evt.filing))
    )
  }

  def filingStatusUpdatedFromProtobuf(
      msg: FilingStatusUpdatedMessage): FilingStatusUpdated = {
    FilingStatusUpdated(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )
  }

  def submissionAddedToProtobuf(
      evt: SubmissionAdded): SubmissionAddedMessage = {
    SubmissionAddedMessage(
      if (evt.submission.isEmpty) None
      else Some(submissionToProtobuf(evt.submission))
    )
  }

  def submissionAddedFromProtobuf(
      msg: SubmissionAddedMessage): SubmissionAdded = {
    SubmissionAdded(
      submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )
  }
}
