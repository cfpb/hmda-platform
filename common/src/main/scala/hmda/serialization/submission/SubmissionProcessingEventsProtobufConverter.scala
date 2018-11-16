package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
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

  def hmdaParserErrorStateToProtobuf(hmdaParserErrorState: HmdaParserErrorState)
    : HmdaParserErrorStateMessage = {
    HmdaParserErrorStateMessage(
      hmdaParserErrorState.transmittalSheetErrors.map(x =>
        hmdaRowParsedErrorToProtobuf(x)),
      hmdaParserErrorState.larErrors.map(x => hmdaRowParsedErrorToProtobuf(x)),
      hmdaParserErrorState.totalErrors
    )
  }

  def hmdaParserErrorStateFromProtobuf(
      hmdaParserErrorStateMessage: HmdaParserErrorStateMessage)
    : HmdaParserErrorState = {
    HmdaParserErrorState(
      hmdaParserErrorStateMessage.transmittalSheetErrors.map(x =>
        hmdaRowParsedErrorFromProtobuf(x)),
      hmdaParserErrorStateMessage.larErrors.map(x =>
        hmdaRowParsedErrorFromProtobuf(x)),
      hmdaParserErrorStateMessage.totalErrors
    )
  }
}
