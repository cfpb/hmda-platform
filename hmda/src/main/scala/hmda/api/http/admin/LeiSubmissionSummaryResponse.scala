package hmda.api.http.model.admin

import hmda.model.filing.submission.SubmissionId
import io.circe.Codec
import io.circe.generic.semiauto._

case class LeiSubmissionSummaryResponse(lei: String, totalSubmissions: Int, submissions: List[SubmissionSummaryResponse])
object LeiSubmissionSummaryResponse {
  implicit val codec: Codec[LeiSubmissionSummaryResponse] = deriveCodec[LeiSubmissionSummaryResponse]
}

case class SubmissionSummaryResponse(submissionId: String, status: Int, totalLines: Int, totalEditCount: Int)
object SubmissionSummaryResponse {
  implicit val codec: Codec[SubmissionSummaryResponse] = deriveCodec[SubmissionSummaryResponse]
}