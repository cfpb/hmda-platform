package hmda.api.model

import hmda.model.fi.Filing
import hmda.model.institution.Institution

case class InstitutionDetail(
  institution: Institution,
  filings: Seq[Filing]
)
