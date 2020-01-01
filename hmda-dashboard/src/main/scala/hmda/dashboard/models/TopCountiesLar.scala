package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class TopCountiesLar(
                      county: String,
                      count: Int
                    )

object TopCountiesLar {
  implicit val getResults: GetResult[TopCountiesLar] = GetResult(r => TopCountiesLar(r.<<,r.<<))

  implicit val codec: Codec[TopCountiesLar] =
    Codec.forProduct2("County","Lar Count")(TopCountiesLar.apply)(f => (f.county,f.count))
}
