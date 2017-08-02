package hmda.publication.reports.protocol

import hmda.model.publication.reports.BorrowerCharacteristic
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class BorrowerCharacteristicProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with BorrowerCharacteristicProtocol {

  property("Borrower Characteristic must convert to and from JSON") {
    forAll(borrowerCharacteristicGen) { b =>
      b.toJson.convertTo[BorrowerCharacteristic] mustBe b
    }
  }
}
