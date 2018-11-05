package hmda.model.institution

import hmda.model.filing.{Filing, FilingDetails}
import hmda.model.institution.Institution

case class InstitutionDetail(
    institution: Option[Institution],
    filings: List[Filing]
)
