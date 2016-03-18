package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Respondent
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V105Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  def respondentNotEmpty(r: Respondent): Boolean = {
    r.id != "" &&
      r.name != "" &&
      r.address != "" &&
      r.city != "" &&
      r.state != "" &&
      r.zipCode != ""
  }

  property("Respondent name, address, city, state and zip code must not be blank") {
    forAll(tsGen) { ts =>
      val r = ts.respondent
      whenever(respondentNotEmpty(r)) {
        V105(r) mustBe Success()
      }
    }
  }

}
