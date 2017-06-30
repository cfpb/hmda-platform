package hmda.api.protocol.public

import hmda.api.model.public.{ InstitutionSearch, InstitutionSearchResults }
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.model.institution.ExternalId

trait InstitutionSearchProtocol extends WriteInstitutionProtocol {
  implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
  implicit val institutionSearchFormat = jsonFormat4(InstitutionSearch.apply)
  implicit val institutionSearchResultFormat = jsonFormat1(InstitutionSearchResults.apply)
}
