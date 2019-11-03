package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilerInformation(lei: String, respondentName: String, year: Int)

object FilerInformation {
  implicit val getResult: GetResult[FilerInformation] = GetResult(r => FilerInformation(r.<<, r.<<, r.<<))

  implicit val codec: Codec[FilerInformation] =
    Codec.forProduct3("lei", "name", "period")(FilerInformation.apply)(f => (f.lei, f.respondentName, f.year))
}
