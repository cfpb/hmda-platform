package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult
import hmda.dataBrowser.models.FilerInformation

case class FilerInformation2017(arid: String, respondentName: String, count: Int ,year: Int) extends FilerInformation

object FilerInformation2017 {
  implicit val getResult: GetResult[FilerInformation2017] = GetResult(r => FilerInformation2017(r.<<, r.<<, r.<<, r.<<))

  implicit val codec: Codec[FilerInformation2017] =
    Codec.forProduct4("arid", "name", "count","period")(FilerInformation2017.apply)(f => (f.arid, f.respondentName, f.count, f.year))
}
