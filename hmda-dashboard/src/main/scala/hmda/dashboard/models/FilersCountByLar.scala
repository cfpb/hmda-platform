package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersCountByLar(
                             count: Int
                           )

object FilersCountByLar {
  implicit val getResults: GetResult[FilersCountByLar] = GetResult(r => FilersCountByLar(r.<<))

  implicit val codec: Codec[FilersCountByLar] =
    Codec.forProduct1("Count")(FilersCountByLar.apply)(f => (f.count))
}

