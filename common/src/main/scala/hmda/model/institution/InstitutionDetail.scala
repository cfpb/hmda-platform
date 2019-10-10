package hmda.model.institution

import hmda.model.filing.Filing
import io.circe._
import io.circe.generic.semiauto._

case class InstitutionDetail(
                              institution: Option[Institution],
                              filings: List[Filing]
                            )

object InstitutionDetail {
  implicit val encoder: Encoder[InstitutionDetail] =
    deriveEncoder[InstitutionDetail]
}