package hmda.publication.reports.protocol

import hmda.model.publication.reports.CharacteristicEnum
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait CharacteristicEnumProtocol extends DefaultJsonProtocol {

  implicit object CharacteristicFormat extends RootJsonFormat[CharacteristicEnum] {
    override def write(obj: CharacteristicEnum): JsValue = JsString(obj.entryName)

    override def read(json: JsValue): CharacteristicEnum = json match {
      case JsString(name) =>
        try {
          CharacteristicEnum.withName(name)
        } catch {
          case _: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid ReportType value: $name"
          )
        }
      case _ => throw DeserializationException("Unable to deserialize")
    }
  }
}
