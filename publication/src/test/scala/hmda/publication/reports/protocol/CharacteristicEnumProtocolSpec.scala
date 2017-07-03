package hmda.publication.reports.protocol

import hmda.model.publication.reports.CharacteristicEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class CharacteristicEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with CharacteristicEnumProtocol {

  forAll(characteristicEnumGen) { c =>
    c.toJson.convertTo[CharacteristicEnum] mustBe c
  }
}
