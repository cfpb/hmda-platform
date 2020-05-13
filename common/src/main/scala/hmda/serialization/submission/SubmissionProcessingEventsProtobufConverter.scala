package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.filing.submission.{ SubmissionStatus, VerificationStatus }
import hmda.model.processing.state.{ EditSummary, HmdaParserErrorState, HmdaValidationErrorState }
import hmda.model.validation.MacroValidationError
import hmda.persistence.serialization.submission.SubmissionIdMessage
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage,
  _
}
import hmda.persistence.serialization.validation.ValidationErrorMessage
import hmda.serialization.submission.SubmissionProtobufConverter._
import hmda.serialization.validation.ValidationProtobufConverter._
import hmda.serialization.submission.SubmissionProcessingCommandsProtobufConverter._

// $COVERAGE-OFF$
object SubmissionProcessingEventsProtobufConverter {

  def hmdaRowParsedErrorToProtobuf(hmdaRowParsedError: HmdaRowParsedError): HmdaRowParsedErrorMessage =
    HmdaRowParsedErrorMessage(
      hmdaRowParsedError.rowNumber,
      hmdaRowParsedError.estimatedULI,
      hmdaRowParsedError.errorMessages.map(x =>
        persistFieldParserErrorToProtobuf(x))
    )

  def hmdaRowParsedErrorFromProtobuf(hmdaRowParsedErrorMessage: HmdaRowParsedErrorMessage): HmdaRowParsedError =
    HmdaRowParsedError(
      hmdaRowParsedErrorMessage.rowNumber,
      hmdaRowParsedErrorMessage.estimatedULI,
      hmdaRowParsedErrorMessage.errors.toList.map(x =>
        persistFieldParserErrorFromProtobuf(x))
    )

  def hmdaRowParsedCountToProtobuf(hmdaRowParsedCount: HmdaRowParsedCount): HmdaRowParsedCountMessage =
    HmdaRowParsedCountMessage(
      hmdaRowParsedCount.count
    )

  def hmdaRowParsedCountFromProtobuf(hmdaRowParsedCountMessage: HmdaRowParsedCountMessage): HmdaRowParsedCount =
    HmdaRowParsedCount(
      hmdaRowParsedCountMessage.count
    )

  def hmdaParserErrorStateToProtobuf(hmdaParserErrorState: HmdaParserErrorState): HmdaParserErrorStateMessage =
    HmdaParserErrorStateMessage(
      hmdaParserErrorState.transmittalSheetErrors.map(x => hmdaRowParsedErrorToProtobuf(x)),
      hmdaParserErrorState.larErrors.map(x => hmdaRowParsedErrorToProtobuf(x)),
      hmdaParserErrorState.totalErrors
    )

  def hmdaParserErrorStateFromProtobuf(hmdaParserErrorStateMessage: HmdaParserErrorStateMessage): HmdaParserErrorState =
    HmdaParserErrorState(
      hmdaParserErrorStateMessage.transmittalSheetErrors
        .map(x => hmdaRowParsedErrorFromProtobuf(x))
        .toList,
      hmdaParserErrorStateMessage.larErrors
        .map(x => hmdaRowParsedErrorFromProtobuf(x))
        .toList,
      hmdaParserErrorStateMessage.totalErrors
    )

  def editSummaryToProtobuf(editSummary: EditSummary): EditSummaryMessage =
    EditSummaryMessage(
      editSummary.editName,
      validationErrorTypeToProtobuf(editSummary.editType),
      validationErrorEntityToProtobuf(editSummary.entityType)
    )

  def editSummaryFromProtobuf(msg: EditSummaryMessage): EditSummary =
    EditSummary(
      msg.editName,
      validationErrorTypeFromProtobuf(msg.validationErrorType),
      validationErrorEntityFromProtobuf(msg.validationErrorEntity)
    )

  def hmdaValidationErrorStateToProtobuf(cmd: HmdaValidationErrorState): HmdaValidationErrorStateMessage =
    HmdaValidationErrorStateMessage(
      cmd.statusCode,
      cmd.syntactical.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.validity.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.quality.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.`macro`.map(s => editSummaryToProtobuf(s)).toSeq,
      cmd.qualityVerified,
      cmd.macroVerified
    )

  def hmdaValidationErrorStateFromProtobuf(msg: HmdaValidationErrorStateMessage): HmdaValidationErrorState =
    HmdaValidationErrorState(
      msg.statusCode,
      msg.syntactical.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.validity.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.quality.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.`macro`.map(m => editSummaryFromProtobuf(m)).toSet,
      msg.qualityVerified,
      msg.macroVerified
    )

  def qualityVerifiedToProtobuf(evt: QualityVerified): QualityVerifiedMessage =
    QualityVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.verified,
      evt.status.code
    )

  def qualityVerifiedFromProtobuf(msg: QualityVerifiedMessage): QualityVerified =
    QualityVerified(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.verified,
      SubmissionStatus.valueOf(msg.statusCode)
    )

  def macroVerifiedToProtobuf(evt: MacroVerified): MacroVerifiedMessage =
    MacroVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.verified,
      evt.status.code
    )

  def macroVerifiedFromProtobuf(msg: MacroVerifiedMessage): MacroVerified =
    MacroVerified(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.verified,
      SubmissionStatus.valueOf(msg.statusCode)
    )

  def notReadyToBeVerifiedToProtobuf(evt: NotReadyToBeVerified): NotReadyToBeVerifiedMessage =
    NotReadyToBeVerifiedMessage(
      submissionIdToProtobuf(evt.submissionId)
    )

  def notReadyToBeVerifiedFromProtobuf(msg: NotReadyToBeVerifiedMessage): NotReadyToBeVerified =
    NotReadyToBeVerified(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )

  def syntacticalValidityCompletedToProtobuf(evt: SyntacticalValidityCompleted): SyntacticalValidityCompletedMessage =
    SyntacticalValidityCompletedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.statusCode
    )

  def syntacticalValidityCompletedFromProtobuf(msg: SyntacticalValidityCompletedMessage): SyntacticalValidityCompleted =
    SyntacticalValidityCompleted(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.statusCode
    )

  def qualityCompletedToProtobuf(evt: QualityCompleted): QualityCompletedMessage =
    QualityCompletedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.statusCode
    )

  def qualityCompletedFromProtobuf(msg: QualityCompletedMessage): QualityCompleted =
    QualityCompleted(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.statusCode
    )

  def macroCompletedToProtobuf(evt: MacroCompleted): MacroCompletedMessage =
    MacroCompletedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.statusCode
    )

  def macroCompletedFromProtobuf(msg: MacroCompletedMessage): MacroCompleted =
    MacroCompleted(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.statusCode
    )

  def submissionSignedToProtobuf(evt: SubmissionSigned): SubmissionSignedMessage =
    SubmissionSignedMessage(
      submissionIdToProtobuf(evt.submissionId),
      evt.timestamp,
      evt.status.code
    )

  def submissionSignedFromProtobuf(msg: SubmissionSignedMessage): SubmissionSigned =
    SubmissionSigned(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage())),
      msg.timestamp,
      SubmissionStatus.valueOf(msg.statusCode)
    )

  def verificationStatusToProtobuf(cmd: VerificationStatus): VerificationStatusMessage =
    VerificationStatusMessage(cmd.qualityVerified, cmd.macroVerified)

  def verificationStatusFromProtobuf(msg: VerificationStatusMessage): VerificationStatus =
    VerificationStatus(msg.qualityVerified, msg.macroVerified)

  def submissionNotReadyToBeSignedToProtobuf(cmd: SubmissionNotReadyToBeSigned): SubmissionNotReadyToBeSignedMessage =
    SubmissionNotReadyToBeSignedMessage(
      submissionIdToProtobuf(cmd.submissionId)
    )

  def submissionNotReadyToBeSignedFromProtobuf(msg: SubmissionNotReadyToBeSignedMessage): SubmissionNotReadyToBeSigned =
    SubmissionNotReadyToBeSigned(
      submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )

  def hmdaMacroValidatedErrorToProtobuf(
    evt: HmdaMacroValidatedError
  ): HmdaMacroValidatedErrorMessage =
    HmdaMacroValidatedErrorMessage(
      Some(validationErrorToProtobuf(evt.error))
    )

  def hmdaMacroValidatedErrorFromProtobuf(msg: HmdaMacroValidatedErrorMessage): HmdaMacroValidatedError =
    HmdaMacroValidatedError(
      validationErrorFromProtobuf(msg.validationError.getOrElse(ValidationErrorMessage()))
        .asInstanceOf[MacroValidationError]
    )

}
// $COVERAGE-ON$
