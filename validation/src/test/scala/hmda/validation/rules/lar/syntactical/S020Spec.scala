package hmda.validation.rules.lar.syntactical

import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.ts.syntactical.S020

class S020Spec extends LarEditCheckSpec {
  property("Loan Application Register Agency Code must = 1,2,3,5,7,9") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        lar.mustPass
      }
    }
  }

  override def check = S020
}
