package hmda.publication

import hmda.model.filing.submission.SubmissionId

sealed trait PublishEvent {
  val key: String
  val value: String
}

case class SignedEvent(submissionId: SubmissionId) extends PublishEvent {
  override val key: String   = "sign"
  override val value: String = s"$submissionId"
}
