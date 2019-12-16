package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class TopFilers(
                      institution_name: String,
                      lei: String,
                      count: Int
                    )


object TopFilers {
  implicit val getResults: GetResult[TopFilers] = GetResult(r => TopFilers(r.<<,r.<<,r.<<))

  implicit val codec: Codec[TopFilers] =
    Codec.forProduct3("name","lei","count")(TopFilers.apply)(f => (f.institution_name,f.lei,f.count))
}
