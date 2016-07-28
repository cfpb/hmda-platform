package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionDetail, InstitutionSummary, Institutions }
import hmda.model.fi._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

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

  implicit object PossibleInstitutionJsonFormat extends RootJsonFormat[PossibleInstitution] {
    override def write(pi: PossibleInstitution): JsValue = {
      pi match {
        case InstitutionNotFound => JsString("Instituion Not Found")
        case Institution(_, _, _) => JsString(jsonFormat3(Institution.apply).toString)
      }
    }

    override def read(json: JsValue): PossibleInstitution = {
      json match {
        case JsString(s) => s match {
          case "Instituion Not Found" => InstitutionNotFound
          case jsonFormat2 => Institution()
        }
        case _ => throw new DeserializationException("Institution Not Found Expected")
      }
    }
  }

  implicit val institutionFormat = jsonFormat3(Institution.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
