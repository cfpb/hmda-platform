package hmda.publication.lar.database

import hmda.model.filing.submission.SubmissionId
import monix.eval.Task

trait EmailSubmissionStatusRepository {
  def findBySubmissionId(submissionId: SubmissionId): Task[Option[EmailSubmissionStatus]]

  def recordEmailSent(e: EmailSubmissionMetadata): Task[EmailSubmissionStatus]

  def recordEmailFailed(e: EmailSubmissionMetadata, reason: String): Task[EmailSubmissionStatus]
}

case class EmailSubmissionMetadata(submissionId: SubmissionId, emailAddress: String)
case class EmailSubmissionStatus(lei: String,
                                 year: Int,
                                 submissionId: String,
                                 emailAddress: String,
                                 successful: Boolean,
                                 failureReason: Option[String])