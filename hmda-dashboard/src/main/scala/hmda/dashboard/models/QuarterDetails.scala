package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class QuarterDetails(
                      q1:  Int,
                      q2:  Int,
                      q3:  Int,
                      q4:  Int
                    )

object QuarterDetails {
  implicit val getResults: GetResult[QuarterDetails] = GetResult(r => QuarterDetails(r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[QuarterDetails] =
    Codec.forProduct4("q1", "q2", "q3", "q4")(QuarterDetails.apply)(f => (f.q1,f.q2,f.q3,f.q4))
}
