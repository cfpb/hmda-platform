package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.MultipleLarEditCheckSpec

class S040Spec extends MultipleLarEditCheckSpec {

  property("Loan/Application number must be unique") {
    forAll(larListGen) { lars =>
      lars.mustPass
      whenever(lars.nonEmpty) {
        val duplicateLars = lars.head :: lars
        duplicateLars.mustFail
      }
    }
  }

  override def check: EditCheck[Iterable[LoanApplicationRegister]] = S040

}
