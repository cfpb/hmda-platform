package hmda.api.http.model.admin

import io.circe.Codec
import io.circe.generic.semiauto._

case class InstitutionDeletedResponse(LEI: String)

object InstitutionDeletedResponse {
  implicit val codec: Codec[InstitutionDeletedResponse] =
    deriveCodec[InstitutionDeletedResponse]
}