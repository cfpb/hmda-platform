package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V682_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V682_1

  property("Loan term must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(loan = lar.loan.copy(loanTerm = "test"))
      invalidLar.mustFail
    }
  }
}
