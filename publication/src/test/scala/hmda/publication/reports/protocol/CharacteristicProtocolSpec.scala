package hmda.publication.reports.protocol

import hmda.model.publication.reports.Characteristic
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class CharacteristicProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with CharacteristicProtocol {

  property("Borrower Characteristic must convert to and from JSON") {
    forAll(characteristicGen) { b =>
      b.toJson.convertTo[Characteristic] mustBe b
    }
  }
}
