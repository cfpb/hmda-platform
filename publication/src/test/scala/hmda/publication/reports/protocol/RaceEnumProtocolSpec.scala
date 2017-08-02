package hmda.publication.reports.protocol

import hmda.model.publication.reports.RaceEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class RaceEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with RaceEnumProtocol {

  property("Race Enum must convert to and from JSON") {
    forAll(raceEnumGen) { r =>
      r.toJson.convertTo[RaceEnum] mustBe r
    }
  }
}
