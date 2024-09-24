package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q651_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q651_1

  property(
    "The CLTV reported is greater than 0 but less than 1.5") {

    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "0.5")).mustFail
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.4")).mustFail
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.5")).mustPass
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "2.5")).mustPass
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "300")).mustPass

    }
  }
}