package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q651_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q651_1

  property(
    "CLTV should not be between 0 and 1.5") {
    
    forAll(larGen) { lar =>
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "NA")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "0")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.5")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.49")).mustFail
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = ".01")).mustFail

    }
  }
}