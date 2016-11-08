package hmda.api.protocol.processing

import hmda.api.model._
import hmda.model.institution.{ Active, Inactive, InstitutionStatus }
import hmda.model.institution.InstitutionStatusMessage._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait InstitutionProtocol extends DefaultJsonProtocol with FilingProtocol {

  implicit object InstitutionStatusJsonFormat extends RootJsonFormat[InstitutionStatus] {

    override def write(status: InstitutionStatus): JsValue = {
      JsObject(
        ("code", JsNumber(status.code)),
        ("message", JsString(status.message))
      )
    }

    override def read(json: JsValue): InstitutionStatus = {
      json.asJsObject.getFields("message").head match {
        case JsString(s) => s match {
          case `activeMsg` => Active
          case `inactiveMsg` => Inactive
          case _ => throw new DeserializationException("Institution Status expected")
        }
        case _ => throw new DeserializationException("Unable to deserialize")
      }
    }

  }

  implicit val institutionWrapperFormat = jsonFormat3(InstitutionWrapper.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
