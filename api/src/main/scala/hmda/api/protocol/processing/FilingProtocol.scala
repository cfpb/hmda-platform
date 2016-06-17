package hmda.api.protocol.processing

import hmda.api.model.{ Filings, InstitutionSummary }
import hmda.model.fi._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait FilingProtocol extends DefaultJsonProtocol {
  implicit object FilingStatusJsonFormat extends RootJsonFormat[FilingStatus] {
    override def read(json: JsValue): FilingStatus = {
      json match {
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
      status match {
        case NotStarted => JsString("not-started")
        case InProgress => JsString("in-progress")
        case Completed => JsString("completed")
        case Cancelled => JsString("cancelled")
      }
    }
  }

  implicit val filingFormat = jsonFormat3(Filing.apply)
  implicit val filingsFormat = jsonFormat1(Filings.apply)
  implicit val insitutionsSummaryFormat = jsonFormat3(InstitutionSummary.apply)
}
