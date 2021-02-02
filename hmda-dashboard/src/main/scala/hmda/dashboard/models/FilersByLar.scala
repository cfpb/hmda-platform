package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersByLar(
                      institution_name: String,
                      lei: String,
                      count: Int
                    )

object FilersByLar {
  implicit val getResults: GetResult[FilersByLar] = GetResult(r => FilersByLar(r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilersByLar] =
    Codec.forProduct3("Name","LEI","Lar Count")(FilersByLar.apply)(f => (f.institution_name,f.lei,f.count))
}
