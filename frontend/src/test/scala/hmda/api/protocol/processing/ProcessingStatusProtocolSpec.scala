package hmda.api.protocol.processing

import hmda.api.model.ModelGenerators
import hmda.api.model.processing.ProcessingStatus
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class ProcessingStatusProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ProcessingStatusProtocol {
  property("status must convert to and from json") {
    forAll(processingStatusGen) { (p) =>
      whenever(p.dateStarted != "") {
        p.toJson.convertTo[ProcessingStatus] mustBe (p)
      }
    }

  }
}
