package hmda.persistence.processing

import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages.{ Command, Event }

object ProcessingMessages {
  case object StartUpload extends Command
  case object CompleteUpload extends Command

  case class UploadStarted(submissionId: SubmissionId) extends Event
  case class UploadCompleted(size: Int, submissionId: SubmissionId) extends Event
}
