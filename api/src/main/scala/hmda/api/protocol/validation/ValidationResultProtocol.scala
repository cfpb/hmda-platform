package hmda.api.protocol.validation

import hmda.api.model.SingleValidationErrorResult
import hmda.validation.engine._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ValidationResultProtocol extends DefaultJsonProtocol {
  implicit object ValidationErrorTypeFormat extends RootJsonFormat[ValidationErrorType] {
    override def read(json: JsValue): ValidationErrorType = {
      json match {
        case JsString(s) => s match {
          case "syntactical" => Syntactical
          case "validity" => Validity
          case "quality" => Quality
          case "macro" => Macro
        }
        case _ => throw DeserializationException("ValidationErrorType expected")
      }
    }
    override def write(errorType: ValidationErrorType): JsValue = {
      errorType match {
        case Syntactical => JsString("syntactical")
        case Validity => JsString("validity")
        case Quality => JsString("quality")
        case Macro => JsString("macro")
      }
    }
  }

  implicit object ValidationErrorFormat extends RootJsonFormat[ValidationError] {
    override def read(json: JsValue): ValidationError = json.asJsObject.fields.getOrElse("errorType", JsString("")) match {
      case JsString("syntactical") => syntacticalValidationErrorFormat.read(json)
      case JsString("validity") => validityValidationErrorFormat.read(json)
      case JsString("quality") => qualityValidationErrorFormat.read(json)
      case JsString("macro") => macroValidationErrorFormat.read(json)
      case _ => throw DeserializationException("ValidationError expected")
    }
    override def write(error: ValidationError): JsValue = error.errorType match {
      case Syntactical => syntacticalValidationErrorFormat.write(error.asInstanceOf[SyntacticalValidationError])
      case Validity => validityValidationErrorFormat.write(error.asInstanceOf[ValidityValidationError])
      case Quality => qualityValidationErrorFormat.write(error.asInstanceOf[QualityValidationError])
      case Macro => macroValidationErrorFormat.write(error.asInstanceOf[MacroValidationError])
    }
  }

  implicit val syntacticalValidationErrorFormat = jsonFormat3(SyntacticalValidationError.apply)
  implicit val validityValidationErrorFormat = jsonFormat3(ValidityValidationError.apply)
  implicit val qualityValidationErrorFormat = jsonFormat3(QualityValidationError.apply)
  implicit val macroValidationErrorFormat = jsonFormat1(MacroValidationError.apply)
  implicit val larValidationErrorsFormat = jsonFormat1(LarValidationErrors.apply)
  implicit val tsValidationErrorsFormat = jsonFormat1(TsValidationErrors.apply)
  implicit val validationErrorsSummaryFormat = jsonFormat1(ValidationErrorsSummary.apply)
  implicit val singleValidationResultFormat = jsonFormat3(SingleValidationErrorResult.apply)
}
