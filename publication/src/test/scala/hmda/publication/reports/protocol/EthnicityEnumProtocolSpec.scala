package hmda.publication.reports.protocol

import hmda.model.publication.reports.EthnicityEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class EthnicityEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with EthnicityEnumProtocol {

  property("Ethnicity Enum must convert to and from JSON") {
    forAll(ethnicityEnumGen) { e =>
      e.toJson.convertTo[EthnicityEnum] mustBe e
    }
  }
}
