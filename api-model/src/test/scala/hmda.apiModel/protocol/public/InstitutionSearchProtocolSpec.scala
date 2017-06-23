package hmda.apiModel.protocol.public

import hmda.apiModel.model.ModelGenerators
import hmda.apiModel.model.public.InstitutionSearch
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class InstitutionSearchProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with InstitutionSearchProtocol {

  property("Institution Search should convert to and from json") {
    forAll(institutionSearchGen) { institution =>
      institution.toJson.convertTo[InstitutionSearch] mustBe institution
    }
  }

  property("Institution search results should convert to and from json") {
    forAll(institutionSearchGenList) { institutions =>
      institutions.toJson.convertTo[List[InstitutionSearch]] mustBe institutions
    }
  }

}
