package hmda.publication.reports.protocol

import hmda.model.publication.reports.MSAReport
import spray.json._

trait MSAReportProtocol extends DefaultJsonProtocol {

  implicit object MSAReportFormat extends RootJsonFormat[MSAReport] {

    override def write(obj: MSAReport): JsValue = {
      JsObject(
        "id" -> JsString(obj.id),
        "name" -> JsString(obj.name),
        "state" -> JsString(obj.state),
        "state_name" -> JsString(obj.stateName)
      )
    }

    override def read(json: JsValue): MSAReport = json match {
      case JsObject(fields) if fields.isDefinedAt("id")
        & fields.isDefinedAt("name")
        & fields.isDefinedAt("state")
        & fields.isDefinedAt("state_name") =>
        MSAReport(
          fields("id").convertTo[String],
          fields("name").convertTo[String],
          fields("state").convertTo[String],
          fields("state_name").convertTo[String]
        )
    }
  }
}
