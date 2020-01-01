package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class SingleAttempts(count: Int)

object SingleAttempts {
  implicit val getResults: GetResult[SingleAttempts] = GetResult(r => SingleAttempts(r.<<))

  implicit val codec: Codec[SingleAttempts] =
    Codec.forProduct1("Single Attempts")(SingleAttempts.apply)(f => (f.count))

}
