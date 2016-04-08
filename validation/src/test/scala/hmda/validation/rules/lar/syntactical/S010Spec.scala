package hmda.validation.rules.lar.syntactical

import hmda.validation.dsl.Success
import hmda.validation.rules.lar.LarEditCheckSpec

class S010Spec extends LarEditCheckSpec {

  property("Record identifier must be 2") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        S010(lar) mustBe Success()
      }
    }
  }
}
