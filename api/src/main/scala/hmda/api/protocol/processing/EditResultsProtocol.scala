package hmda.api.protocol.processing

import hmda.api.model._
import hmda.validation.engine._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait EditResultsProtocol extends DefaultJsonProtocol {

  implicit object ValidationErrorTypeJsonFormat extends RootJsonFormat[ValidationErrorType] {
    override def write(errorType: ValidationErrorType): JsValue = {
      errorType match {
        case Syntactical => JsString("syntactical")
        case Validity => JsString("validity")
        case Quality => JsString("quality")
        case Macro => JsString("macro")
      }
    }

    override def read(json: JsValue): ValidationErrorType = {
      json match {
        case JsString(s) => s match {
          case "syntactical" => Syntactical
          case "validity" => Validity
          case "quality" => Quality
          case "macro" => Macro
        }
        case _ => throw new DeserializationException("Validation Error Type expected")
      }
    }
  }

  implicit val larIdFormat = jsonFormat1(LarId.apply)
  implicit val larEditResultFormat = jsonFormat1(LarEditResult.apply)
  implicit val editResultFormat = jsonFormat2(EditResult.apply)
  implicit val editResultsFormat = jsonFormat1(EditResults.apply)
  implicit val summaryEditResultsFormat = jsonFormat4(SummaryEditResults.apply)

}
