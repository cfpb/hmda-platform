package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class TopInstitutionsCountOpenEndCredit(
                                            agency_code: Int,
                                            lei: String,
                                            institution_name: String
                    )

object TopInstitutionsCountOpenEndCredit {
  implicit val getResults: GetResult[TopInstitutionsCountOpenEndCredit] = GetResult(r => TopInstitutionsCountOpenEndCredit(r.<<,r.<<,r.<<))

  implicit val codec: Codec[TopInstitutionsCountOpenEndCredit] =
    Codec.forProduct3("Agency Code","LEI","Institution Name")(TopInstitutionsCountOpenEndCredit.apply)(f => (f.agency_code,f.lei,f.institution_name))
}
