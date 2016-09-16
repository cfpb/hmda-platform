package hmda.api.protocol.processing

import hmda.api.model.{ EditResults, ModelGenerators }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class EditResultsProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with EditResultsProtocol {

  property("EditResults must convert to and from json") {
    forAll(editResultsGen) { e =>
      e.toJson.convertTo[EditResults] mustBe e
    }
  }
}
