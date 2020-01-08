package hmda.api.http.model.filing.submissions

import io.circe.Decoder
import io.circe.generic.semiauto._

case class EditsSign(signed: Boolean)

object EditsSign {
  implicit val decoder: Decoder[EditsSign] = deriveDecoder[EditsSign]
}