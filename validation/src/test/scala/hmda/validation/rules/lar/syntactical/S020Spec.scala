package hmda.validation.rules.lar.syntactical

import hmda.validation.dsl.Success
import hmda.validation.rules.lar.LarEditCheckSpec

class S020Spec extends LarEditCheckSpec {
  property("Loan Application Register Agency Code must = 1,2,3,5,7,9") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        S020(lar) mustBe Success()
      }
    }
  }
}
