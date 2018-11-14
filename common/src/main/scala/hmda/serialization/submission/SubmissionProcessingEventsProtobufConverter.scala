package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError,
  PersistedHmdaRowParsedError
}
import hmda.model.processing.state.{
  EditSummary,
  HmdaParserErrorState,
  HmdaValidationErrorState
}
import hmda.model.validation.{ValidationErrorEntity, ValidationErrorType}
import hmda.persistence.serialization.submission.processing.events._
import hmda.serialization.validation.ValidationProtobufConverter._

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

  def editSummaryToProtobuf(editSummary: EditSummary): EditSummaryMessage = {
    EditSummaryMessage(
      editSummary.editName,
      validationErrorTypeToProtobuf(editSummary.editType),
      validationErrorEntityToProtobuf(editSummary.entityType)
    )
  }

  def editSummaryFromProtobuf(msg: EditSummaryMessage): EditSummary = {
    EditSummary(
      msg.editName,
      validationErrorTypeFromProtobuf(msg.validationErrorType),
      validationErrorEntityFromProtobuf(msg.validationErrorEntity)
    )
  }

  def hmdaValidationErrorStateToProtobuf(
      cmd: HmdaValidationErrorState): HmdaValidationErrorStateMessage = {
    HmdaValidationErrorStateMessage(
      cmd.syntactical.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.validity.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.quality.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.`macro`.map(s => editSummaryToProtobuf(s)).toSeq
    )
  }

  def hmdaValidationErrorStateFromProtobuf(
      msg: HmdaValidationErrorStateMessage): HmdaValidationErrorState = {
    HmdaValidationErrorState(
      msg.syntactical.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.validity.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.quality.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.syntactical.map(m => editSummaryFromProtobuf(m)).toSet
    )
  }

}
