package hmda.publication.reports.protocol

import hmda.model.publication.reports.MinorityStatusEnum
import hmda.model.publication.reports.MinorityStatusEnum.{ OtherIncludingHispanic, WhiteNonHispanic }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait MinorityStatusEnumProtocol extends DefaultJsonProtocol {

  implicit object MinorityStatusEnumFormat extends RootJsonFormat[MinorityStatusEnum] {
    override def write(obj: MinorityStatusEnum): JsValue = JsString(obj.description)

    override def read(json: JsValue): MinorityStatusEnum = json match {
      case JsString(description) => description match {
        case WhiteNonHispanic.description => WhiteNonHispanic
        case OtherIncludingHispanic.description => OtherIncludingHispanic
        case _ => throw DeserializationException(s"Unable to translate JSON string into valid Action Type value: $description")
      }
      case _ => throw DeserializationException("Unable to deserialize")
    }
  }

}
