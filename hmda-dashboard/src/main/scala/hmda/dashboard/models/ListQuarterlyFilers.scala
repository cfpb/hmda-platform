package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class ListQuarterlyFilers(
                                lei: String,
                                agency: Int,
                                institution_name: String,
                              )

object ListQuarterlyFilers {
  implicit val getResults: GetResult[ListQuarterlyFilers] = GetResult(r => ListQuarterlyFilers(r.<<,r.<<,r.<<))

  implicit val codec: Codec[ListQuarterlyFilers] =
    Codec.forProduct3("LEI","Agency","Institution Name")(ListQuarterlyFilers.apply)(f => (f.lei,f.agency,f.institution_name))
}
