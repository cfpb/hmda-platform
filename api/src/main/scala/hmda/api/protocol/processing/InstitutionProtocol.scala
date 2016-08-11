package hmda.api.protocol.processing

import hmda.api.model.{InstitutionDetail, InstitutionSummary, Institutions}
import hmda.model.institution.DepositoryType.{Depository, NonDepository}
import hmda.model.institution.{Agency, DepositoryType, Institution, InstitutionStatus}
import hmda.model.institution.InstitutionStatus.{Active, Inactive}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

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
    override def write(agency: Agency): JsValue = JsObject(
      "value" -> JsNumber(agency.value),
      "name" -> JsString(agency.name),
      "fullName" -> JsString(agency.fullName)
    )

    override def read(json: JsValue): Agency = {
      json.asJsObject.getFields("value", "name", "fullName") match {
        //TODO: Read in Agency value
        case _ => throw new DeserializationException("Agency expected")
      }
    }
  }

  implicit object DepositoryJsonFormat extends RootJsonFormat[DepositoryType] {
    override def write(depositoryType: DepositoryType): JsValue = {
      depositoryType match {
        case Depository => JsString("depository")
        case NonDepository => JsString("nondepository")
      }
    }

    override def read(json: JsValue): DepositoryType = {
      json match {
        case JsString(s) => s match {
          case "depository" => Depository
          case "nondepository" => NonDepository
        }
        case _ => throw new DeserializationException("Depository Type expected")
      }
    }
  }

  implicit object InstitutionJsonFormat extends RootJsonFormat[Institution] {
    override def write(institution: Institution): JsValue = JsObject(
      "id" -> JsNumber(institution.id),
      "name" -> JsString(institution.name),
      "agency" -> JsString("") //TODO
    )

    override def read(json: JsValue): Institution = {
      json.asJsObject.getFields("id", "name", "agency", "status") match {
        //TODO: Read in Institution JSON
        case _ => throw new DeserializationException("Institution Status expected")
      }
    }
  }

  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
  implicit val institutionDetail = jsonFormat2(InstitutionDetail.apply)

}
