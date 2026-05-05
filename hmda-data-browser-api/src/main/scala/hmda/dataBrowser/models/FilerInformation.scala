package hmda.dataBrowser.models

import io.circe.Codec
import slick.jdbc.GetResult

trait FilerInformation

case class FilerInformationLatest(lei: String, respondentName: String, count: Int, year: Int) extends FilerInformation

// $COVERAGE-OFF$
object FilerInformationLatest {
  implicit val getResult: GetResult[FilerInformationLatest] = GetResult(r => FilerInformationLatest(r.<<, r.<<, r.<<, r.<<))

  implicit val codec: Codec[FilerInformationLatest] =
    Codec.forProduct4("lei", "name", "count", "period")(FilerInformationLatest.apply)(f => (f.lei.trim, f.respondentName.trim, f.count, f.year))
}
// $COVERAGE-ON$