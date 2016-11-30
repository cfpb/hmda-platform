package hmda.api.protocol.validation

import hmda.api.model.{ SingleValidationErrorResult, VerifyLarErrorResponse }
import hmda.persistence.processing.HmdaFileValidator.VerifyLarError
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
        case _ => throw new DeserializationException("ValidationErrorType expected")
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

  implicit val validationErrorFormat = jsonFormat3(ValidationError.apply)
  implicit val larValidationErrorsFormat = jsonFormat1(LarValidationErrors.apply)
  implicit val tsValidationErrorsFormat = jsonFormat1(TsValidationErrors.apply)
  implicit val validationErrorsSummaryFormat = jsonFormat1(ValidationErrorsSummary.apply)
  implicit val singleValidationResultFormat = jsonFormat3(SingleValidationErrorResult.apply)
  implicit val verifyLarErrorFormat = jsonFormat2(VerifyLarError.apply)
  implicit val larErrorVerifiedResponseFormat = jsonFormat2(VerifyLarErrorResponse.apply)
}
