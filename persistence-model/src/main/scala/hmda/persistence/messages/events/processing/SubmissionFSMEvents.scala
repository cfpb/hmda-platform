package hmda.persistence.messages.events.processing

import hmda.model.fi.Submission
import hmda.persistence.messages.CommonMessages.Event

object SubmissionFSMEvents {

  trait SubmissionEvent extends Event

  case class SubmissionCreated(s: Submission) extends SubmissionEvent
  case class SubmissionUploading(s: Submission) extends SubmissionEvent
  case class SubmissionUploaded(s: Submission) extends SubmissionEvent
  case class SubmissionParsing(s: Submission) extends SubmissionEvent
  case class SubmissionParsed(s: Submission) extends SubmissionEvent
  case class SubmissionParsedWithErrors(s: Submission) extends SubmissionEvent
  case class SubmissionValidating(s: Submission) extends SubmissionEvent
  case class SubmissionValidated(s: Submission) extends SubmissionEvent
  case class SubmissionValidatedWithErrors(s: Submission) extends SubmissionEvent
  case class SubmissionSigned(s: Submission) extends SubmissionEvent
  case class SubmissionFailed(s: Submission) extends SubmissionEvent

}
