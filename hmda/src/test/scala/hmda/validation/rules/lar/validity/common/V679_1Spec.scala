package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V679_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V679_1

  property("Debt to income ratio must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "test")).mustFail
      lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "")).mustFail
      lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "-1.0")).mustPass
    }
  }
}
