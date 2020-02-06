package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q651Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q651

  property(
    "CLTV should not be between 0 and 1") {
    
    forAll(larGen) { lar =>
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "NA")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "0")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1")).mustPass
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = ".99")).mustFail
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = ".01")).mustFail

    }
  }
}
