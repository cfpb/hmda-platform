package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersByLar(
                      institution_name: String,
                      lei: String,
                      count: Int,
                      city: String,
                      state: String,
                      sign_date: String
                    )

object FilersByLar {
  implicit val getResults: GetResult[FilersByLar] = GetResult(r => FilersByLar(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilersByLar] =
    Codec.forProduct6("Name","LEI","Lar Count","City","State","Sign Date")(FilersByLar.apply)(f => (f.institution_name,f.lei,f.count,f.city,f.state,f.sign_date))
}
