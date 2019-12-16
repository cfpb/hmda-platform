package hmda.dashboard.models

import slick.jdbc.GetResult
import io.circe.Codec

case class TotalFilers(count: Int)

object TotalFilers {
  implicit val getResults: GetResult[TotalFilers] = GetResult(r => TotalFilers(r.<<))

  implicit val codec: Codec[TotalFilers] =
    Codec.forProduct1("Total Filers")(TotalFilers.apply)(f => (f.count))
}

