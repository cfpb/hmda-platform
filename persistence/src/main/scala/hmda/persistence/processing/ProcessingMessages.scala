package hmda.persistence.processing

import akka.actor.ActorRef
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event }

object ProcessingMessages {

  //Commands
  case object StartUpload extends Command
  case object CompleteUpload extends Command
  case object StartParsing extends Command
  case object CompleteParsing extends Command
  case object CompleteParsingWithErrors extends Command
  case class BeginValidation(replyTo: ActorRef) extends Command
  case class CompleteValidation(replyTo: ActorRef) extends Command
  case object CompleteValidationWithErrors extends Command
  case object Sign extends Command

  //Events
  case class UploadStarted(submissionId: SubmissionId) extends Event
  case class UploadCompleted(size: Int, submissionId: SubmissionId) extends Event
  case class ParsingStarted(submissionId: SubmissionId) extends Event
  case class ParsingCompleted(submissionId: SubmissionId) extends Event
  case class ParsingCompletedWithErrors(submissionId: SubmissionId) extends Event
  case class ValidationCompleted(submissionId: SubmissionId) extends Event
  case class ValidationCompletedWithErrors(submissionId: SubmissionId) extends Event
  case object Persisted extends Event
  case object Signed extends Event
}
