package hmda.api.http.model.filing.submissions

import io.circe.Decoder
import io.circe.generic.semiauto._

case class EditsVerification(verified: Boolean)

object EditsVerification {
  implicit val decoder: Decoder[EditsVerification] =
    deriveDecoder[EditsVerification]
}