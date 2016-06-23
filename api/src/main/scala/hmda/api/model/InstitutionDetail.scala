package hmda.api.model

import hmda.model.fi.{ Filing, Institution }

case class InstitutionDetail(
  institution: Institution,
  filings: Seq[Filing]
)
