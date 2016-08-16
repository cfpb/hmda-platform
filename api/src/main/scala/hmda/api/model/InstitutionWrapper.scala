package hmda.api.model

import hmda.model.institution.InstitutionStatus

case class InstitutionWrapper(
  id: Int,
  name: String,
  status: InstitutionStatus
)
