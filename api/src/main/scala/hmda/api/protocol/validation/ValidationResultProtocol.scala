package hmda.api.protocol.validation

import hmda.validation.engine.{ ValidationError, ValidationErrors }
import spray.json.DefaultJsonProtocol

trait ValidationResultProtocol extends DefaultJsonProtocol {
  implicit val validationErrorFormat = jsonFormat2(ValidationError.apply)
  implicit val validationErrorsFormat = jsonFormat1(ValidationErrors.apply)
}
