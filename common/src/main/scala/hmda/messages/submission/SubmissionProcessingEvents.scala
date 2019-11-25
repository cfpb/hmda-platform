package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.filing.submission.{SubmissionId, SubmissionStatus}
import hmda.model.validation.{MacroValidationError, ValidationError}
import hmda.messages.submission.SubmissionProcessingCommands.FieldParserError

object SubmissionProcessingEvents {
  sealed trait SubmissionProcessingEvent extends Event

  case class HmdaRowParsedError(rowNumber: Int,
                                estimatedULI: String,
                                errorMessages: List[FieldParserError])
      extends SubmissionProcessingEvent

  case class HmdaRowValidatedError(rowNumber: Int,
                                   validationErrors: List[ValidationError])
    extends SubmissionProcessingEvent

  case class HmdaMacroValidatedError(error: MacroValidationError)
    extends SubmissionProcessingEvent

  case class HmdaRowParsedCount(count: Int) extends SubmissionProcessingEvent

  case class SyntacticalValidityCompleted(submissionId: SubmissionId,
                                          statusCode: Int)
    extends SubmissionProcessingEvent

  case class QualityCompleted(submissionId: SubmissionId, statusCode: Int)
    extends SubmissionProcessingEvent

  case class MacroCompleted(submissionId: SubmissionId, statusCode: Int)
    extends SubmissionProcessingEvent

  case class QualityVerified(submissionId: SubmissionId,
                             verified: Boolean,
                             status: SubmissionStatus)
    extends SubmissionProcessingEvent

  case class MacroVerified(submissionId: SubmissionId,
                           verified: Boolean,
                           status: SubmissionStatus)
    extends SubmissionProcessingEvent

  case class NotReadyToBeVerified(submissionId: SubmissionId)
    extends SubmissionProcessingEvent

  sealed trait SubmissionSignedEvent extends SubmissionProcessingEvent

  case class SubmissionSigned(submissionId: SubmissionId,
                              timestamp: Long,
                              status: SubmissionStatus)
    extends SubmissionSignedEvent {
    def receipt: String = s"$submissionId-$timestamp"
  }

  case class SubmissionNotReadyToBeSigned(submissionId: SubmissionId)
    extends SubmissionSignedEvent

}