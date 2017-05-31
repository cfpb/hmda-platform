package hmda.persistence.messages.events.processing

import hmda.model.fi.Submission
import hmda.persistence.messages.CommonMessages.Event

object SubmissionFSMEvents {

  trait SubmissionFSMEvent extends Event

  case class SubmissionFSMCreated(s: Submission) extends SubmissionFSMEvent
  case class SubmissionUploading(s: Submission) extends SubmissionFSMEvent
  case class SubmissionUploaded(s: Submission) extends SubmissionFSMEvent
  case class SubmissionParsing(s: Submission) extends SubmissionFSMEvent
  case class SubmissionParsed(s: Submission) extends SubmissionFSMEvent
  case class SubmissionParsedWithErrors(s: Submission) extends SubmissionFSMEvent
  case class SubmissionValidating(s: Submission) extends SubmissionFSMEvent
  case class SubmissionValidated(s: Submission) extends SubmissionFSMEvent
  case class SubmissionValidatedWithErrors(s: Submission) extends SubmissionFSMEvent
  case class SubmissionSigned(s: Submission) extends SubmissionFSMEvent
  case class SubmissionFailed(s: Submission) extends SubmissionFSMEvent

}
