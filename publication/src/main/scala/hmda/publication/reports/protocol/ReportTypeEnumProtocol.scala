package hmda.publication.reports.protocol

import hmda.model.publication.reports.ReportTypeEnum
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ReportTypeEnumProtocol extends DefaultJsonProtocol {

  implicit object ReportTypeFormat extends RootJsonFormat[ReportTypeEnum] {

    override def write(obj: ReportTypeEnum): JsValue = JsString(obj.entryName)

    override def read(json: JsValue): ReportTypeEnum = json match {
      case JsString(name) =>
        try {
          ReportTypeEnum.withName(name)
        } catch {
          case _: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid ReportType value: $name"
          )
        }
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }
}
