package hmda.validation.rules.lar.validity._2020

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V677_1_2020Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V677_1

  property("Interest rate must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar.copy(loan = lar.loan.copy(interestRate = "test")).mustFail
      lar.copy(loan = lar.loan.copy(interestRate = "-5")).mustFail
      lar.copy(loan = lar.loan.copy(interestRate = "0")).mustPass
      lar.copy(loan = lar.loan.copy(interestRate = "1.0")).mustPass
    }
  }
}