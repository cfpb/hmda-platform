package hmda.api.protocol.validation

import hmda.api.model.ModelGenerators
import hmda.validation.engine.ValidationError
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class ValidationResultProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ValidationResultProtocol {

  property("validation result must convert to and from json") {
    forAll(validationErrorGen) { e =>
      e.toJson.convertTo[ValidationError] must be(e)
    }
  }

}
