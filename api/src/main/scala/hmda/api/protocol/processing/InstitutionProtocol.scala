package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionDetail, InstitutionWrapper, Institutions }
import hmda.model.institution.DepositoryType.{ Depository, NonDepository }
import hmda.model.institution.{ Agency, DepositoryType, Institution, InstitutionStatus }
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

  implicit val institutionWrapperFormat = jsonFormat3(InstitutionWrapper.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
