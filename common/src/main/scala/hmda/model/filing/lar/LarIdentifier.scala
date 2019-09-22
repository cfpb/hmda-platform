package hmda.model.filing.lar

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class LarIdentifier(id: Int = 2,
                         LEI: String = "",
                         NMLSRIdentifier: String = "")

object LarIdentifier {
  implicit val larIdentifierEncoder: Encoder[LarIdentifier] =
    deriveEncoder[LarIdentifier]
  implicit val larIdentifierDecoder: Decoder[LarIdentifier] =
    deriveDecoder[LarIdentifier]
}