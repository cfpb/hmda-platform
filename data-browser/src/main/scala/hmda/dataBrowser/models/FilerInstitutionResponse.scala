package hmda.dataBrowser.models

import io.circe.Codec

case class FilerInstitutionResponse(institutions: Seq[FilerInformation])

object FilerInstitutionResponse {
  implicit val codec: Codec[FilerInstitutionResponse] =
    Codec.forProduct1("institutions")(FilerInstitutionResponse.apply)(f => f.institutions)

}
