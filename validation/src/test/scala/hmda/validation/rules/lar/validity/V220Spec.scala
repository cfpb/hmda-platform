package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V220Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Loan Type must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        V220(lar.loan) mustBe Success()
      }
    }
  }
}
