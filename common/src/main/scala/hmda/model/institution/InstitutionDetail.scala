package hmda.model.institution

import hmda.model.filing.Filing

case class InstitutionDetail(
  institution: Option[Institution],
  filings: List[Filing]
)
