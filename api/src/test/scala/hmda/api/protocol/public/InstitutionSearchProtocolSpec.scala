package hmda.api.protocol.public

import hmda.api.model.ModelGenerators
import hmda.api.model.public.InstitutionSearch
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class InstitutionSearchProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionSearchProtocol {

  property("Institution Search should convert to and from json") {
    forAll(institutionSearchGen) { institution =>
      institution.toJson.convertTo[InstitutionSearch] mustBe institution
    }
  }

}
