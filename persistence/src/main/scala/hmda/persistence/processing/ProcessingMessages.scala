package hmda.persistence.processing

import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages.{ Command, Event }

object ProcessingMessages {

  //Commands
  case object StartUpload extends Command
  case object CompleteUpload extends Command
  case object StartParsing extends Command
  case object CompleteParsing extends Command
  case object CompleteParsingWithErrors extends Command
  case class UploadStarted(submissionId: SubmissionId) extends Event
  case class UploadCompleted(size: Int, submissionId: SubmissionId) extends Event

  //Events
  case class ParsingStarted(submissionId: SubmissionId) extends Event
  case class ParsingCompleted(submissionId: SubmissionId) extends Event
  case class ParsingCompletedWithErrors(submissionId: SubmissionId) extends Event
}
