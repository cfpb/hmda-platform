package hmda.apiModel.model.public

import hmda.model.institution.ExternalId

case class InstitutionSearch(
  id: String,
  name: String,
  domains: Set[String],
  externalIds: Set[ExternalId]
)

case class InstitutionSearchResults(institutions: Set[InstitutionSearch])
