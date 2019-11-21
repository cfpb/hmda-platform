package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilerInformation(lei: String, respondentName: String, count: Int ,year: Int)

object FilerInformation {
  implicit val getResult: GetResult[FilerInformation] = GetResult(r => FilerInformation(r.<<, r.<<, r.<<, r.<<))

  implicit val codec: Codec[FilerInformation] =
    Codec.forProduct4("lei", "name", "count","period")(FilerInformation.apply)(f => (f.lei, f.respondentName, f.count, f.year))
}
