package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersForLastDays (
                             date: String,
                             count: Int
                             )

object FilersForLastDays {
  implicit val getResults: GetResult[FilersForLastDays] = GetResult(r => FilersForLastDays(r.<<,r.<<))

  implicit val codec: Codec[FilersForLastDays] =
    Codec.forProduct2("date","count")(FilersForLastDays.apply)(f => (f.date,f.count))
}

