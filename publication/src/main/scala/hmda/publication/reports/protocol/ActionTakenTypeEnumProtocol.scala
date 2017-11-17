package hmda.publication.reports.protocol

import hmda.model.publication.reports.DispositionEnum
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ActionTakenTypeEnumProtocol extends DefaultJsonProtocol {

  implicit object ActionTakenTypeEnumFormat extends RootJsonFormat[DispositionEnum] {

    override def write(obj: DispositionEnum): JsValue = JsString(obj.value)

    override def read(json: JsValue): DispositionEnum =
      json match {
        case JsString(description) => DispositionEnum.withValue(description)
        case _ => throw DeserializationException("Unable to deserialize")

      }

  }

}
