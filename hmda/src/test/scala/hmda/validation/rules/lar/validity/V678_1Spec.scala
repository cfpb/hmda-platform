package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V678_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V678_1

  property("Prepayment penalty term must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar
        .copy(loan = lar.loan.copy(prepaymentPenaltyTerm = "test"))
        .mustFail
      lar
        .copy(loan = lar.loan.copy(prepaymentPenaltyTerm = "-10.0"))
        .mustFail
    }
  }
}
