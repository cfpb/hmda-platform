package hmda.api.protocol

import hmda.api.model.ModelGenerators
import hmda.api.protocol.processing.InstitutionProtocol
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.fi.{ Institution, InstitutionStatus }
import spray.json._

class InstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionProtocol {

  property("Institution status must convert to and from json") {
    forAll(institutionStatusGen) { p =>
      p.toJson.convertTo[InstitutionStatus] mustBe (p)
    }
  }

  property("An Institution must convert to and from json") {
    forAll(institutionGen) { i =>
      i.toJson.convertTo[Institution] mustBe (i)
    }
  }

}
