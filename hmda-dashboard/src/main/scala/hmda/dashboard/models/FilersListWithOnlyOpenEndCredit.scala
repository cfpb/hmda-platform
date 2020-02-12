package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersListWithOnlyOpenEndCredit(
                                    lei: String,
                                    agency: Int,
                                    institution_name: String,
                                  )

object FilersListWithOnlyOpenEndCredit {
  implicit val getResults: GetResult[FilersListWithOnlyOpenEndCredit] = GetResult(r => FilersListWithOnlyOpenEndCredit(r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilersListWithOnlyOpenEndCredit] =
    Codec.forProduct3("LEI","Agency","Institution Name")(FilersListWithOnlyOpenEndCredit.apply)(f => (f.lei,f.agency,f.institution_name))
}
