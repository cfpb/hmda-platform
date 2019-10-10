package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.{SubmissionStatus, VerificationStatus}
import io.circe.{Encoder, Json}

case class SubmissionStatusResponse(submissionStatus: SubmissionStatus,
                                    verification: VerificationStatus)

object SubmissionStatusResponse {
  implicit val encoder: Encoder[SubmissionStatusResponse] = {
    a: SubmissionStatusResponse =>
      val s = a.submissionStatus
      val v = a.verification
      Json.obj(
        ("code", Json.fromInt(s.code)),
        ("message", Json.fromString(s.message)),
        ("description", Json.fromString(s.description)),
        ("qualityVerified", Json.fromBoolean(v.qualityVerified)),
        ("macroVerified", Json.fromBoolean(v.macroVerified))
      )
  }
}