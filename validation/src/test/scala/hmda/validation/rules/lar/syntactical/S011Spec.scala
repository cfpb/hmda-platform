package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.MultipleLarEditCheckSpec

class S011Spec extends MultipleLarEditCheckSpec {
  property("LAR list must not be empty") {
    forAll(larListGen) { lars =>
      lars.mustPass
    }
  }

  override def check: EditCheck[Iterable[LoanApplicationRegister]] = S011
}
