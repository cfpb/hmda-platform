package hmda.api.protocol.admin

import hmda.api.model.ModelGenerators
import hmda.model.institution.InstitutionGenerators._
import hmda.model.institution.Institution
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class WriteInstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with WriteInstitutionProtocol {

  property("Institution should convert to and from json") {
    forAll(institutionGen) { institution =>
      institution.toJson.convertTo[Institution] mustBe institution
    }
  }

}
