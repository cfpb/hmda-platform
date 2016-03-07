package hmda.validation.rules.lar.syntactical

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S020Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Loan Application Register Agency Code must = 1,2,3,5,7,9") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        S020(lar) mustBe Success()
      }
    }
  }
}
