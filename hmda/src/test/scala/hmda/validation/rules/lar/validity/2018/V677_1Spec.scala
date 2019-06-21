package hmda.validation.rules.lar.validity.eighteen

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity.eighteen.V677_1

class V677_1_2018Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V677_1

  property("Interest rate must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar.copy(loan = lar.loan.copy(interestRate = "test")).mustFail
      lar.copy(loan = lar.loan.copy(interestRate = "-5")).mustFail
      lar.copy(loan = lar.loan.copy(interestRate = "0")).mustFail
      lar.copy(loan = lar.loan.copy(interestRate = "1.0")).mustPass
    }
  }
}
