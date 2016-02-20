package hmda.validation.rules.syntactical

import hmda.parser.fi.lar.LarGenerators
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S020Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with LarGenerators {

  property("Transmittal Sheet Agency Code must = 1,2,3,5,7,9") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S020(ts) mustBe Success()
      }
    }
  }

  property("Loan Application Register Agency Code must = 1,2,3,5,7,9")(pending)
}
