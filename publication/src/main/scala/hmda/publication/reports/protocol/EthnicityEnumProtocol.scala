package hmda.publication.reports.protocol

import hmda.model.publication.reports.EthnicityEnum
import hmda.model.publication.reports.EthnicityEnum.{ HispanicOrLatino, Joint, NotAvailable, NotHispanicOrLatino }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait EthnicityEnumProtocol extends DefaultJsonProtocol {

  implicit object EthnicityEnumFormat extends RootJsonFormat[EthnicityEnum] {

    override def write(obj: EthnicityEnum): JsValue = JsString(obj.description)

    override def read(json: JsValue): EthnicityEnum = json match {
      case JsString(description) => description match {
        case HispanicOrLatino.description => HispanicOrLatino
        case NotHispanicOrLatino.description => NotHispanicOrLatino
        case NotAvailable.description => NotAvailable
        case Joint.description => Joint
        case _ => throw DeserializationException(s"Unable to translate JSON string into valid Action Type value: $description")
      }
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }
}
