package hmda.api.protocol.validation

import hmda.api.model.ModelGenerators
import hmda.model.validation.ValidationErrorGenerators._
import hmda.model.validation._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class ValidationResultProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ValidationResultProtocol {
  property("syntactical validation error must convert to and from json") {
    forAll(syntacticalValidationErrorGen) { e =>
      e.toJson.convertTo[SyntacticalValidationError] must be(e)
    }
  }

  property("validity validation error must convert to and from json") {
    forAll(validityValidationErrorGen) { e =>
      e.toJson.convertTo[ValidityValidationError] must be(e)
    }
  }

  property("quality validation error must convert to and from json") {
    forAll(qualityValidationErrorGen) { e =>
      e.toJson.convertTo[QualityValidationError] must be(e)
    }
  }

  property("macro validation error must convert to and from json") {
    forAll(macroValidationErrorGen) { e =>
      e.toJson.convertTo[MacroValidationError] must be(e)
    }
  }

}
