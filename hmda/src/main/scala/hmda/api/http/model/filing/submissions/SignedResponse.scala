package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus
import io.circe.Encoder
import io.circe.generic.semiauto._


case class SignedResponse(email: String = "dev@email.com", timestamp: Long, receipt: String, status: SubmissionStatus, signerUsername: Option[String])

object SignedResponse {
  implicit val encoder: Encoder[SignedResponse] = deriveEncoder[SignedResponse]
}