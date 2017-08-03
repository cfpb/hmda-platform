package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class S010Spec extends LarEditCheckSpec {

  property("Record identifier must be 2") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        lar.mustPass
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = S010
}
