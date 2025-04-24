package hmda.authService.model

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._

case class UserUpdate(firstName: String, lastName: String, leis: List[String])

object UserUpdate {
  implicit val decoder: Decoder[UserUpdate] = deriveDecoder[UserUpdate]
  implicit val encoder: Encoder[UserUpdate] = deriveEncoder[UserUpdate]
}