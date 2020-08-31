package hmda.model.filing.submission

import io.circe.Codec
import io.circe.generic.semiauto._

case class Submission(
                       id: SubmissionId = SubmissionId(),
                       status: SubmissionStatus = Created,
                       start: Long = 0,
                       end: Long = 0,
                       fileName: String = "",
                       receipt: String = "",
                       signerUsername: Option[String] = None
                     ) {
  def isEmpty: Boolean =
    id == SubmissionId() && status == Created && start == 0 && end == 0 && fileName == "" && receipt == "" && signerUsername.isEmpty
}

object Submission {
  import io.circe.generic.auto._
  implicit val codec: Codec[Submission] = deriveCodec[Submission]
}