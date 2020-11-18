package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class LateFilers(
                      agency: Int,
                      institution_name: String,
                      lei: String,
                      total_lines: String,
                      sign_date: String,
                      sub_id: String
                      )

object LateFilers {
  implicit val getResults: GetResult[LateFilers] = GetResult(r => LateFilers(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<))

  implicit val codec: Codec[LateFilers] =
    Codec.forProduct6("Agency","Institution Name","LEI","Total Lars","Sign Date (EST)","Submission ID")(LateFilers.apply)(f => (f.agency,f.institution_name,f.lei,f.total_lines,f.sign_date,f.sub_id))
}