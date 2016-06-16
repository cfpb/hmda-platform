package hmda.api.protocol.processing

import hmda.api.model.ModelGenerators
import hmda.model.fi.Filing
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class FilingProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with FilingProtocol {

  property("Filing must convert to and from json") {
    forAll(filingGen) { f =>
      f.toJson.convertTo[Filing] mustBe f
    }
  }

}
