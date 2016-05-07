package hmda.validation.rules.lar.syntactical

import hmda.validation.dsl.Success
import hmda.validation.rules.lar.MultipleLarEditCheckSpec

class S011Spec extends MultipleLarEditCheckSpec {
  property("LAR list must not be empty") {
    forAll(larListGen) { lars =>
      S011(lars) mustBe Success()
    }
  }
}
