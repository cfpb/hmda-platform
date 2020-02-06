package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q652Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q652

  property(
    "DTI should not be between 0 and 1") {
    
    forAll(larGen) { lar =>
        lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "NA")).mustPass
        lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "0")).mustPass
        lar.copy(loan = lar.loan.copy(debtToIncomeRatio = "1")).mustPass
        lar.copy(loan = lar.loan.copy(debtToIncomeRatio = ".99")).mustFail
        lar.copy(loan = lar.loan.copy(debtToIncomeRatio = ".01")).mustFail

    }
  }
}
