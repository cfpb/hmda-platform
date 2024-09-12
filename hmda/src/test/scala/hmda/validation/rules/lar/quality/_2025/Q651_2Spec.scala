package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q651_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q651-2

  property(
    "CLTV should not be between 0 and 1") {
    
    forAll(larGen) { lar =>
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "NA")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "149.99")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "0")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.50")).mustFail
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.51")).mustFail

    }
  }
}