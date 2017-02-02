package hmda.api.protocol.public

import hmda.api.model.public.InstitutionSearch
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.model.institution.{ ExternalId, Institution }
import spray.json._

trait InstitutionSearchProtocol extends WriteInstitutionProtocol {
  implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
  implicit object ExternalIdsJsonFormat extends RootJsonFormat[Set[ExternalId]] {
    override def write(obj: Set[ExternalId]): JsValue = {
      JsArray(obj.toJson)
    }

    override def read(json: JsValue): Set[ExternalId] = throw DeserializationException("Operation not supported")
  }
  implicit val institutionSearchFormat = jsonFormat4(InstitutionSearch.apply)

  //implicit object institutionSetFormat extends RootJsonFormat[Set[Institution]] {
  //  override def write(obj: Set[Institution]): JsValue = JsArray(obj.toJson)

  //  override def read(json: JsValue): Set[Institution] = throw DeserializationException("Operation not supported")
  //}
}
