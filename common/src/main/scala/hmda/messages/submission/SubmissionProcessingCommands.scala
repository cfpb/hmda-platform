package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.messages.submission.ValidationProgressTrackerCommands.ValidationProgressTrackerCommand
import hmda.model.filing.submission.{ SubmissionId, VerificationStatus }
import hmda.model.processing.state.{ HmdaParserErrorState, HmdaValidationErrorState }
import hmda.model.validation.{ MacroValidationError, ValidationError }

object SubmissionProcessingCommands {
  sealed trait SubmissionProcessingCommand extends Command

  case class TrackProgress(replyTo: ActorRef[ActorRef[ValidationProgressTrackerCommand]]) extends SubmissionProcessingCommand

  case class StartUpload(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class CompleteUpload(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class StartParsing(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class FieldParserError(fieldName: String, inputValue: String)

  case class PersistHmdaRowParsedError(
      rowNumber: Int,
      estimatedULI: String,
      errors: List[FieldParserError],
      maybeReplyTo: Option[ActorRef[HmdaRowParsedError]])
      extends SubmissionProcessingCommand

  case class GetParsedWithErrorCount(replyTo: ActorRef[SubmissionProcessingEvent]) extends SubmissionProcessingCommand

  case class GetParsingErrors(page: Int, replyTo: ActorRef[HmdaParserErrorState]) extends SubmissionProcessingCommand

  case class CompleteParsing(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class CompleteParsingWithErrors(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class StartSyntacticalValidity(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class PersistHmdaRowValidatedError(
    submissionId: SubmissionId,
    rowNumber: Int,
    validationErrors: List[ValidationError],
    replyTo: Option[ActorRef[HmdaRowValidatedError]]
  ) extends SubmissionProcessingCommand

  case class PersistMacroError(
    submissionId: SubmissionId,
    validationError: MacroValidationError,
    maybeReplyTo: Option[ActorRef[MacroValidationError]]
  ) extends SubmissionProcessingCommand

  case class GetHmdaValidationErrorState(submissionId: SubmissionId, replyTo: ActorRef[HmdaValidationErrorState])
      extends SubmissionProcessingCommand

  case class GetVerificationStatus(replyTo: ActorRef[VerificationStatus]) extends SubmissionProcessingCommand

  case class CompleteSyntacticalValidity(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class StartQuality(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class StartMacro(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class CompleteQuality(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class CompleteMacro(submissionId: SubmissionId) extends SubmissionProcessingCommand

  case class VerifyQuality(submissionId: SubmissionId, verified: Boolean, replyTo: ActorRef[SubmissionProcessingEvent])
      extends SubmissionProcessingCommand

  case class VerifyMacro(submissionId: SubmissionId, verified: Boolean, replyTo: ActorRef[SubmissionProcessingEvent])
      extends SubmissionProcessingCommand

  case class SignSubmission(submissionId: SubmissionId, replyTo: ActorRef[SubmissionSignedEvent], email: String, signerUsername: String)
      extends SubmissionProcessingCommand

  case object HmdaParserStop extends SubmissionProcessingCommand

}
