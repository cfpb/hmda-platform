package hmda.validation.rules.ts.syntactical

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S020Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal Sheet Agency Code must = 1,2,3,5,7,9") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S020(ts) mustBe Success()
      }
    }
  }

}
