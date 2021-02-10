package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilerAllPeriods(
                      year: String,
                      institution_name: String,
                      lei: String,
                      count: Int,
                      city: String,
                      state: String,
                      sign_date: String,
                      agency: Int
                    )


object FilerAllPeriods {
  implicit val getResults: GetResult[FilerAllPeriods] = GetResult(r => FilerAllPeriods(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilerAllPeriods] =
    Codec.forProduct8("year","Name","LEI","Lar Count","City","State","Sign Date","agency")(FilerAllPeriods.apply)(f => (f.year,f.institution_name,f.lei,f.count,f.city,f.state,f.sign_date,f.agency))
}
