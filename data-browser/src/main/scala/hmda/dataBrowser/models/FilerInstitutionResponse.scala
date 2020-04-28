package hmda.dataBrowser.models

import io.circe.Codec

case class FilerInstitutionResponse2017(institutions: Seq[FilerInformation2017])

case class FilerInstitutionResponse2018(institutions: Seq[FilerInformation2018])

object FilerInstitutionResponse2017 {
  implicit val codec: Codec[FilerInstitutionResponse2017] =
    Codec.forProduct1("institutions")(FilerInstitutionResponse2017.apply)(f => f.institutions)

}

object FilerInstitutionResponse2018 {
  implicit val codec: Codec[FilerInstitutionResponse2018] =
    Codec.forProduct1("institutions")(FilerInstitutionResponse2018.apply)(f => f.institutions)
}
