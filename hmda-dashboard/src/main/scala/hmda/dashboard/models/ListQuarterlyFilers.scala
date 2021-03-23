package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class ListQuarterlyFilers(
                                lei: String,
                                agency: Int,
                                institution_name: String,
                                sign_date: String,
                                sign_date_utc: String,
                                sign_date_east: String,
                                total_lines: Int
                              )

object ListQuarterlyFilers {
  implicit val getResults: GetResult[ListQuarterlyFilers] = GetResult(r => ListQuarterlyFilers(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[ListQuarterlyFilers] =
    Codec.forProduct7("LEI","Agency","Institution Name","Sign Date","Sign Date UTC","Sign Date EST", "Total Lars")(ListQuarterlyFilers.apply)(f => (f.lei,f.agency,f.institution_name,f.sign_date,f.sign_date_utc,f.sign_date_east,f.total_lines))
}
