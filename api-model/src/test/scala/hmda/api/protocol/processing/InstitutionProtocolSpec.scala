package hmda.api.protocol.processing

import hmda.api.model.{ InstitutionWrapper, ModelGenerators }
import hmda.model.institution.InstitutionGenerators._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._

class InstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionProtocol {

  property("An Institution must convert to a JSON wrapper") {
    forAll(institutionGen) { i =>
      InstitutionWrapper(i.id, i.respondent.name).toJson mustBe
        JsObject(
          ("id", JsString(i.id)),
          ("name", JsString(i.respondent.name))
        )
    }
  }

}
