package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError,
  PersistedHmdaRowParsedError
}
import hmda.model.processing.state.{
  HmdaParserErrorState,
  HmdaValidationErrorState
}
import hmda.persistence.serialization.submission.processing.commands.HmdaValidationErrorStateMessage
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage,
  PersistedHmdaRowParsedErrorMessage
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

  def persistedHmdaRowParsedToProtobuf(
      evt: PersistedHmdaRowParsedError): PersistedHmdaRowParsedErrorMessage = {
    PersistedHmdaRowParsedErrorMessage(
      evt.rowNumber,
      evt.errors
    )
  }

  def persistedHmdaRowParsedFromProtobuf(
      msg: PersistedHmdaRowParsedErrorMessage): PersistedHmdaRowParsedError = {
    PersistedHmdaRowParsedError(
      msg.rowNumber,
      msg.errors.toList
    )
  }

  def hmdaValidationErrorStateToProtobuf(
      cmd: HmdaValidationErrorState): HmdaValidationErrorStateMessage = {
    HmdaValidationErrorStateMessage(
      cmd.totalErrors,
      cmd.syntacticalErrors,
      cmd.validityErrors,
      cmd.qualityErrors,
      cmd.macroErrors
    )
  }

  def hmdaValidationErrorStateFromProtobuf(
      msg: HmdaValidationErrorStateMessage): HmdaValidationErrorState = {
    HmdaValidationErrorState(
      msg.totalErrors,
      msg.syntacticalErrors,
      msg.validityErrors,
      msg.qualityErrors,
      msg.macroErrors
    )
  }

}
