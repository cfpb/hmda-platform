package hmda.api.http.model.public

import io.circe.Decoder
import io.circe.generic.semiauto._

case class LarValidateRequest(
                               lar: String
                             )

object LarValidateRequest {
  implicit val decoder: Decoder[LarValidateRequest] =
    deriveDecoder[LarValidateRequest]
}