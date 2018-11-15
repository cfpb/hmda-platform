package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.processing.state.{EditSummary, HmdaValidationErrorState}
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage
}
import hmda.persistence.serialization.submission.processing.events._
import hmda.serialization.validation.ValidationProtobufConverter._
import SubmissionProtobufConverter._
import hmda.persistence.serialization.submission.SubmissionIdMessage

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
      cmd.statusCode,
      cmd.syntactical.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.validity.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.quality.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.`macro`.map(s => editSummaryToProtobuf(s)).toSeq
    )
  }

  def hmdaValidationErrorStateFromProtobuf(
      msg: HmdaValidationErrorStateMessage): HmdaValidationErrorState = {
    HmdaValidationErrorState(
      msg.statusCode,
      msg.syntactical.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.validity.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.quality.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.syntactical.map(m => editSummaryFromProtobuf(m)).toSet
    )
  }

  def qualityVerifiedToProtobuf(
      evt: QualityVerified): QualityVerifiedMessage = {
    QualityVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId)
    )
  }

  def qualityVerifiedFromProtobuf(
      msg: QualityVerifiedMessage): QualityVerified = {
    QualityVerified(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())))
  }

  def macroVerifiedToProtobuf(evt: MacroVerified): MacroVerifiedMessage = {
    MacroVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId)
    )
  }

  def macroVerifiedFromProtobuf(msg: MacroVerifiedMessage): MacroVerified = {
    MacroVerified(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())))
  }

  def notReadyToBeVerifiedToProtobuf(
      evt: NotReadyToBeVerified): NotReadyToBeVerifiedMessage = {
    NotReadyToBeVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId)
    )
  }

  def notReadyToBeVerifiedFromProtobuf(
      msg: NotReadyToBeVerifiedMessage): NotReadyToBeVerified = {
    NotReadyToBeVerified(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }

  def syntacticalValidityCompletedToProtobuf(evt: SyntacticalValidityCompleted)
    : SyntacticalValidityCompletedMessage = {
    SyntacticalValidityCompletedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.statusCode
    )
  }

  def syntacticalValidityCompletedFromProtobuf(
      msg: SyntacticalValidityCompletedMessage)
    : SyntacticalValidityCompleted = {
    SyntacticalValidityCompleted(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.statusCode
    )
  }

  def qualityCompletedToProtobuf(
      evt: QualityCompleted): QualityCompletedMessage = {
    QualityCompletedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.statusCode
    )
  }

  def qualityCompletedFromProtobuf(
      msg: QualityCompletedMessage): QualityCompleted = {
    QualityCompleted(
      submissionIdFromProtobuf(
        msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.statusCode
    )
  }

}
