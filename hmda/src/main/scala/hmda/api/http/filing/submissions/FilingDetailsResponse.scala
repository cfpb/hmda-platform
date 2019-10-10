package hmda.api.http.model.filing.submissions

import hmda.model.filing.Filing
import io.circe.Encoder
import io.circe.generic.semiauto._

case class FilingDetailsResponse(filing: Filing = Filing(),
                                 submissions: List[SubmissionResponse])

object FilingDetailsResponse {
  implicit val encoder: Encoder[FilingDetailsResponse] =
    deriveEncoder[FilingDetailsResponse]
}