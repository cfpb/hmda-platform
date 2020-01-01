package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class MultipleAttempts(count: Int)

object MultipleAttempts {
  implicit val getResults: GetResult[MultipleAttempts] = GetResult(r => MultipleAttempts(r.<<))

  implicit val codec: Codec[MultipleAttempts] =
    Codec.forProduct1("Multiple Attempts")(MultipleAttempts.apply)(f => (f.count))

}
