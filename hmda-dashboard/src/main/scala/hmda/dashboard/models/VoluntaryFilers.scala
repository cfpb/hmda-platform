package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class VoluntaryFilers(
                      category: String,
                      institutions: Int,
                      count: Int
                      )

object VoluntaryFilers {
  implicit val getResults: GetResult[VoluntaryFilers] = GetResult(r => VoluntaryFilers(r.<<,r.<<,r.<<))

  implicit val codec: Codec[VoluntaryFilers] =
    Codec.forProduct3("Category","Institutions","Total Lar")(VoluntaryFilers.apply)(f => (f.category,f.institutions,f.count))
}