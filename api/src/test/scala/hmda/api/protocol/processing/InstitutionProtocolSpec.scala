package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionSummary, ModelGenerators }
import hmda.model.fi.{ Filing, Institution, InstitutionStatus }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class InstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionProtocol {

  property("Institution status must convert to and from json") {
    forAll(institutionStatusGen) { p =>
      p.toJson.convertTo[InstitutionStatus] mustBe p
    }
  }

  property("An Institution must convert to and from json") {
    forAll(institutionGen) { i =>
      i.toJson.convertTo[Institution] mustBe i
    }
  }

}
