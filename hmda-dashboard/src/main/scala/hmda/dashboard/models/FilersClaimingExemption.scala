package hmda.dashboard.models

import io.circe.Codec
import slick.jdbc.GetResult

case class FilersClaimingExemption(
                      lei: String,
                      agency: Int,
                      institution_name: String,
                    )

object FilersClaimingExemption {
  implicit val getResults: GetResult[FilersClaimingExemption] = GetResult(r => FilersClaimingExemption(r.<<,r.<<,r.<<))

  implicit val codec: Codec[FilersClaimingExemption] =
    Codec.forProduct3("LEI","Agency","Institution Name")(FilersClaimingExemption.apply)(f => (f.lei,f.agency,f.institution_name))
}
