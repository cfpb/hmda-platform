package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionWrapper, ModelGenerators }
import hmda.model.institution.{ InstitutionStatus }
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
      InstitutionWrapper(i.id.toString, i.name, i.status).toJson mustBe
        JsObject(
          ("id", JsString(i.id.toString)),
          ("name", JsString(i.name)),
          ("status", JsObject(
            ("code", JsNumber(i.status.code)),
            ("message", JsString(i.status.message))
          ))
        )
    }
  }

}
