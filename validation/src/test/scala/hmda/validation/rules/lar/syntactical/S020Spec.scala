package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Success
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class S020Spec extends LarEditCheckSpec {
  property("Loan Application Register Agency Code must = 1,2,3,5,7,9") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        lar.mustPass
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = S020
}
