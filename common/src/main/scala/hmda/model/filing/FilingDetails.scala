package hmda.model.filing

import hmda.model.filing.submission.Submission
import io.circe.Encoder
import io.circe.generic.semiauto._

case class FilingDetails(filing: Filing = Filing(),
                         submissions: List[Submission] = Nil)

object FilingDetails {
  implicit val encoder: Encoder[FilingDetails] = deriveEncoder[FilingDetails]
}