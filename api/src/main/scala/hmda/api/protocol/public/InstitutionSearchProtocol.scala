package hmda.api.protocol.public

import hmda.api.model.public.InstitutionSearch
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.model.institution.{ ExternalId, Institution }
import spray.json._

trait InstitutionSearchProtocol extends WriteInstitutionProtocol {
  implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
  implicit val institutionSearchFormat = jsonFormat4(InstitutionSearch.apply)

}
