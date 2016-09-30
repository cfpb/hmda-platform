package hmda.api.protocol.processing

import hmda.api.model.{ ErrorResponse, ModelGenerators }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class ApiErrorProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ApiErrorProtocol {

  property("An API error must convert to and from json") {
    forAll(errorResponseGen) { e =>
      e.toJson.convertTo[ErrorResponse] mustBe e
    }
  }
}
