package hmda.dataBrowser.models

import io.circe.{ Codec, Encoder }
// $COVERAGE-OFF$
object FilerInstitutionResponse2017 {
  implicit val codec: Codec[FilerInstitutionResponse2017] =
    Codec.forProduct1("institutions")(FilerInstitutionResponse2017.apply)(f => f.institutions)

}
case class FilerInstitutionResponse2017(institutions: Seq[FilerInformation2017])

object FilerInstitutionHttpResponse2017 {
  implicit val encoder: Encoder[FilerInstitutionHttpResponse2017] =
    Encoder.forProduct2("institutions", "servedFrom")(f => (f.institutions, f.servedFrom))
}
case class FilerInstitutionHttpResponse2017(institutions: Seq[FilerInformation2017], servedFrom: ServedFrom)

object FilerInstitutionResponseLatest {
  implicit val codec: Codec[FilerInstitutionResponseLatest] =
    Codec.forProduct1("institutions")(FilerInstitutionResponseLatest.apply)(f => f.institutions)
}
case class FilerInstitutionResponseLatest(institutions: Seq[FilerInformationLatest])

object FilerInstitutionHttpResponseLatest {
  implicit val encoder: Encoder[FilerInstitutionHttpResponseLatest] =
    Encoder.forProduct2("institutions", "servedFrom")(f => (f.institutions, f.servedFrom))
}
case class FilerInstitutionHttpResponseLatest(institutions: Seq[FilerInformationLatest], servedFrom: ServedFrom)
// $COVERAGE-ON$