package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import hmda.persistence.serialization.submission.processing.events.{
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage
}

object SubmissionProcessingEventsProtobufConverter {

  def hmdaRowParsedErrorToProtobuf(
      hmdaRowParsedError: HmdaRowParsedError): HmdaRowParsedErrorMessage = {
    HmdaRowParsedErrorMessage(
      hmdaRowParsedError.rowNumber,
      hmdaRowParsedError.errorMessages
    )
  }

  def hmdaRowParsedErrorFromProtobuf(
      hmdaRowParsedErrorMessage: HmdaRowParsedErrorMessage)
    : HmdaRowParsedError = {
    HmdaRowParsedError(
      hmdaRowParsedErrorMessage.rowNumber,
      hmdaRowParsedErrorMessage.errors.toList
    )
  }

  def hmdaRowParsedCountToProtobuf(
      hmdaRowParsedCount: HmdaRowParsedCount): HmdaRowParsedCountMessage = {
    HmdaRowParsedCountMessage(
      hmdaRowParsedCount.count
    )
  }

  def hmdaRowParsedCountFromProtobuf(
      hmdaRowParsedCountMessage: HmdaRowParsedCountMessage)
    : HmdaRowParsedCount = {
    HmdaRowParsedCount(
      hmdaRowParsedCountMessage.count
    )
  }

}
