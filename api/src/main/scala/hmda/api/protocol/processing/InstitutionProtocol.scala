package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionDetail, InstitutionSummary, Institutions }
import hmda.model.institution.{ Agency, Institution, InstitutionStatus }
import hmda.model.institution.InstitutionStatus.{ Active, Inactive }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait InstitutionProtocol extends DefaultJsonProtocol with FilingProtocol {
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

  implicit object AgencyJsonFormat extends RootJsonFormat[Agency] {
    override def write(agency: Agency): JsValue = JsObject(Map(
      "value" -> JsNumber(agency.value),
      "name" -> JsString(agency.name),
      "fullName" -> JsString(agency.fullName),
      "externalIds" -> JsString("")
    ))

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

  override def read(json: JsValue): Agency = {
    json match {
      case JsString(s) => s match {
        case "active" => Active
        case "inactive" => Inactive
      }
      case _ => throw new DeserializationException("Institution Status expected")
    }
  }

  implicit val institutionFormat = jsonFormat6(Institution.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
