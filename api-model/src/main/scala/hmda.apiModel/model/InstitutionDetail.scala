package hmda.apiModel.model

import hmda.model.fi.Filing

case class InstitutionDetail(
  institution: InstitutionWrapper,
  filings: Seq[Filing]
)
