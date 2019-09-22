package hmda.model.filing.submission

import hmda.model.filing.ts.TransmittalSheet
import io.circe.Encoder
import io.circe.generic.semiauto._

case class SubmissionSummary(
                              submission: Option[Submission] = None,
                              ts: Option[TransmittalSheet] = None
                            )

object SubmissionSummary {
  // automatically derive the nested case classes
  import io.circe.generic.auto._

  implicit val encoder: Encoder[SubmissionSummary] =
    deriveEncoder[SubmissionSummary]
}