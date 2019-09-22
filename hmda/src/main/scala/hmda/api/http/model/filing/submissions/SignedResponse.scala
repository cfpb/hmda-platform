package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus
import io.circe._
import io.circe.generic.semiauto._

case class SignedResponse(timestamp: Long,
                          receipt: String,
                          status: SubmissionStatus)

object SignedResponse {
  implicit val encoder: Encoder[SignedResponse] = deriveEncoder[SignedResponse]
}