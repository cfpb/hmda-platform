package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q650_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q650_2

  property("Interest rate should not be greater than 20") {
    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(interestRate = "21.05")).mustFail
    }
  }
}
