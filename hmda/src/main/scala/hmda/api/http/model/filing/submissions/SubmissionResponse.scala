package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.{ QualityMacroExists, Submission, VerificationStatus }
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{ Encoder, Json }

case class SubmissionResponse(submission: Submission, verificationStatus: VerificationStatus, qualityMacroExists: QualityMacroExists)

object SubmissionResponse {
  implicit val encoder: Encoder[SubmissionResponse] = { r =>
    val s = r.submission.asJsonObject.toList
    val v = r.verificationStatus.asJsonObject.toList
    val c = r.qualityMacroExists.asJsonObject.toList
    Json.fromFields(s ++ v ++ c)
  }

}
