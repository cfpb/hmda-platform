package hmda.api.http.model.admin

import io.circe.Codec
import io.circe.generic.semiauto._

import scala.collection.immutable.ListMap

/** ListMap is used instead of Map to keep the ordering (the entries should be sorted by year) */
case class LeiSubmissionSummaryResponse(lei: String, years: ListMap[String, YearlySubmissionSummaryResponse])
object LeiSubmissionSummaryResponse {
  implicit val codec: Codec[LeiSubmissionSummaryResponse] = deriveCodec[LeiSubmissionSummaryResponse]
}

case class LeiLatestSubmissionSummaryResponse(lei: String, latestSubmission: SubmissionSummaryResponse)
object LeiLatestSubmissionSummaryResponse {
  implicit val codec: Codec[LeiLatestSubmissionSummaryResponse] = deriveCodec[LeiLatestSubmissionSummaryResponse]
}

case class YearlySubmissionSummaryResponse(totalSubmissions: Int, submissions: List[SubmissionSummaryResponse])
object YearlySubmissionSummaryResponse {
  implicit val codec: Codec[YearlySubmissionSummaryResponse] = deriveCodec[YearlySubmissionSummaryResponse]
}

case class SubmissionSummaryResponse(submissionId: String, status: Int, totalLines: Int, validity: Int, syntactical: Int, quality: Int, `macro`: Int)
object SubmissionSummaryResponse {
  implicit val codec: Codec[SubmissionSummaryResponse] = deriveCodec[SubmissionSummaryResponse]
}