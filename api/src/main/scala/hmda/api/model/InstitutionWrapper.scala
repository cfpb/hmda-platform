package hmda.api.model

import hmda.model.institution.InstitutionStatus

case class InstitutionWrapper(
  id: String,
  name: String,
  status: InstitutionStatus
)
