package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionSummary, InstitutionWrapper, ModelGenerators }
import hmda.model.institution.{ Institution, InstitutionStatus }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class InstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionProtocol {

  property("Institution status must convert to and from json") {
    forAll(institutionStatusGen) { p =>
      p.toJson.convertTo[InstitutionStatus] mustBe p
    }
  }

  property("An Institution must convert to a JSON wrapper") {
    forAll(institutionGen) { i =>
      InstitutionWrapper(i.id, i.name, i.status).toJson mustBe
        JsObject(("id", JsNumber(i.id.toString)), ("name", JsString(i.name)), ("status", JsString(i.status.entryName)))
    }
  }

}
