package hmda.api.model

import hmda.model.fi.{ Filing, Institution, PossibleInstitution, InstitutionNotFound }

sealed trait PossibleInstitutionDetail

case class InstitutionDetailNotFound(
  filings: Seq[Filing]
)

case class InstitutionDetail(
  institution: Institution,
  filings: Seq[Filing]
) extends PossibleInstitutionDetail
