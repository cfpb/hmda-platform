package hmda.query.model.institutions

import hmda.query.dao.HmdaEntity

case class InstitutionEntity(
  id: String = "",
  name: String = "",
  cra: Boolean = false,
  agency: Int = 0,
  institutionType: String = "",
  parent: Boolean = false,
  status: Int = 0,
  filingPeriod: Int = 0
) extends HmdaEntity
