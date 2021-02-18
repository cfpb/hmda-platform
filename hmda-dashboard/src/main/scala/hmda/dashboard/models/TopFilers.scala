package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class TopFilers(
                      institution_name: String,
                      lei: String,
                      count: Int,
                      city: String,
                      state: String,
                      sign_date: String
                    )

object TopFilers {
  implicit val getResults: GetResult[TopFilers] = GetResult(r => TopFilers(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[TopFilers] =
    Codec.forProduct6("Name","LEI","Lar Count","City","State","Sign Date")(TopFilers.apply)(f => (f.institution_name,f.lei,f.count,f.city,f.state,f.sign_date))
}
