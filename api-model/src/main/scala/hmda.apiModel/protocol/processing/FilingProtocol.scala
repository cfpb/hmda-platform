package hmda.apiModel.protocol.processing

import hmda.apiModel.model.{ FilingDetail, Filings }
import hmda.model.fi._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait FilingProtocol extends DefaultJsonProtocol with SubmissionProtocol {
  implicit object FilingStatusJsonFormat extends RootJsonFormat[FilingStatus] {
    override def read(json: JsValue): FilingStatus = {
      json.asJsObject.getFields("message").head match {
        case JsString(s) => s match {
          case "not-started" => NotStarted
          case "in-progress" => InProgress
          case "completed" => Completed
          case "cancelled" => Cancelled
        }
        case _ => throw new DeserializationException("Filing Status expected")
      }
    }

    override def write(status: FilingStatus): JsValue = {
      JsObject(
        "code" -> JsNumber(status.code),
        "message" -> JsString(status.message)
      )
    }
  }

  implicit val filingFormat = jsonFormat6(Filing.apply)
  implicit val filingsFormat = jsonFormat1(Filings.apply)
  implicit val filingDetailFormat = jsonFormat2(FilingDetail.apply)
}
