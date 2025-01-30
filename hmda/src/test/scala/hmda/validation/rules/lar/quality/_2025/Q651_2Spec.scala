package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q651_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q651_2

  property(
    "CLTV should be under 150") {

    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "NA")).mustPass
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "149")).mustPass
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "0")).mustPass
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "150")).mustFail
      lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "151")).mustFail

    }
  }
}