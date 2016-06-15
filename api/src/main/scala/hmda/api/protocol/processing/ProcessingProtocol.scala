package hmda.api.protocol.processing

import hmda.api.model.{ Active, Inactive, Institution, InstitutionStatus }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ProcessingProtocol extends DefaultJsonProtocol {
  implicit object InstitutionStatusJsonFormat extends RootJsonFormat[InstitutionStatus] {
    override def write(status: InstitutionStatus): JsValue = {
      status match {
        case Active => JsString("active")
        case Inactive => JsString("inactive")
      }
    }

    override def read(json: JsValue): InstitutionStatus = {
      json match {
        case JsString(s) => s match {
          case "active" => Active
          case "inactive" => Inactive
        }
        case _ => throw new DeserializationException("Institution Status expected")
      }
    }
  }

  implicit val institutionFormat = jsonFormat3(Institution.apply)
}
