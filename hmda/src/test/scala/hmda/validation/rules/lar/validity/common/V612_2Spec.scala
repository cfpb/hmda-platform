package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V612_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V612_2

  property("When preappoval is requested, loan must be home purchase") {
    forAll(larGen) { lar =>
      whenever(lar.action.preapproval == PreapprovalRequested) {
        val homePurchase = lar.loan.copy(loanPurpose = HomePurchase)
        val notHP = lar.loan.copy(loanPurpose = new InvalidLoanPurposeCode)

        lar.copy(loan = homePurchase).mustPass
        lar.copy(loan = notHP).mustFail
      }
    }
  }
}
