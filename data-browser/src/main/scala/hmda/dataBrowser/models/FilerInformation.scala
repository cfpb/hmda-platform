package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

trait FilerInformation

case class FilerInformation2018(lei: String, respondentName: String, count: Int ,year: Int) extends FilerInformation

object FilerInformation2018 {
  implicit val getResult: GetResult[FilerInformation2018] = GetResult(r => FilerInformation2018(r.<<, r.<<, r.<<, r.<<))

  implicit val codec: Codec[FilerInformation2018] =
    Codec.forProduct4("lei", "name", "count","period")(FilerInformation2018.apply)(f => (f.lei, f.respondentName, f.count, f.year))
}
