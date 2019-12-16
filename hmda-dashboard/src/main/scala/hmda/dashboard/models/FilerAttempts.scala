package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilerAttempts (
                           institution_name: String,
                           lar_count: Int,
                           attempts: Int
                         )

object FilerAttempts {
  implicit val getResults: GetResult[FilerAttempts] = GetResult(r => FilerAttempts(r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilerAttempts] =
    Codec.forProduct3("institution_name","lar_count","attempts")(FilerAttempts.apply)(f => (f.institution_name,f.lar_count,f.attempts))
}