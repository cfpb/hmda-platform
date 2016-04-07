package hmda.validation.rules.lar.syntactical

import hmda.validation.dsl.Success
import hmda.validation.rules.lar.LarEditCheckSpec

class S011Spec extends LarEditCheckSpec {
  property("LAR list must not be empty") {
    forAll(larListGen) { lars =>
      S011(lars) mustBe Success()
    }
  }
}
