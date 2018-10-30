package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.submission.SubmissionProcessingEvents.SubmissionProcessingEvent
import hmda.model.filing.HmdaFileRow
import hmda.model.filing.submission.SubmissionId

object SubmissionProcessingCommands {
  sealed trait SubmissionProcessingCommand extends Command
  case class StartUpload(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteUpload(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class StartParsing(submissionId: SubmissionId)
      extends SubmissionProcessingCommand

  case class HmdaRowParsed(hmdaFileRow: HmdaFileRow)
      extends SubmissionProcessingCommand

  case class PersistHmdaRowParsedError(rowNumber: Int, errors: List[String])
      extends SubmissionProcessingCommand

  case class GetParsedWithErrorCount(replyTo: ActorRef[SubmissionProcessingEvent])
      extends SubmissionProcessingCommand

  case class FailProcessing(ex: Throwable) extends SubmissionProcessingCommand

  case class CompleteParsing(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteParsingWithErrors(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class StartSyntacticalValidity(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteSyntacticalValidity(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteSyntacticalValidityWithErrors(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class StartQuality(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteQuality(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteQualityWithErrors(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class StartMacro(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteMacro(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class CompleteMacroWithErrors(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class Verify(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class Sign(submissionId: SubmissionId)
      extends SubmissionProcessingCommand
  case class Fail(submissionId: SubmissionId)
      extends SubmissionProcessingCommand

  case object HmdaParserStop extends SubmissionProcessingCommand

}
