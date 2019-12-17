package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class SignsForLastDays(
                             date: String,
                             count: Int
                             )

object SignsForLastDays {
  implicit val getResults: GetResult[SignsForLastDays] = GetResult(r => SignsForLastDays(r.<<,r.<<))

  implicit val codec: Codec[SignsForLastDays] =
    Codec.forProduct2("Date","Sign Count")(SignsForLastDays.apply)(f => (f.date,f.count))
}

