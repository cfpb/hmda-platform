package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus
import io.circe.Encoder
import io.circe.generic.semiauto._

case class EditsVerificationResponse(verified: Boolean,
                                     status: SubmissionStatus)

object EditsVerificationResponse {
  implicit val encoder: Encoder[EditsVerificationResponse] =
    deriveEncoder[EditsVerificationResponse]
}