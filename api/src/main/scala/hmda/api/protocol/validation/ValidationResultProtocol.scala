package hmda.api.protocol.validation

import hmda.validation.engine.ValidationError
import spray.json.DefaultJsonProtocol

trait ValidationResultProtocol extends DefaultJsonProtocol {
  implicit val validationErrorFormat = jsonFormat2(ValidationError.apply)
}
