package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  HomePurchase,
  PreapprovalNotRequested,
  PreapprovalRequested
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V614_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V614_1

  property("Home purchase purposes must pass") {
    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(loanPurpose = HomePurchase)).mustPass
    }
  }

  property("Non-home purchase purposes must have no preapproval requested") {
    forAll(larGen) { lar =>
      whenever(lar.loan.loanPurpose != HomePurchase) {
        lar
          .copy(action = lar.action.copy(preapproval = PreapprovalNotRequested))
          .mustPass
        lar
          .copy(action = lar.action.copy(preapproval = PreapprovalRequested))
          .mustFail
      }
    }
  }
}
