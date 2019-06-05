package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V614_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V614_3

  property("Non Reverse Mortgages must pass") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage == NotReverseMortgage) {
        lar.mustPass
      }
    }
  }

  property("Reverse mortgages must have no preapproval requested") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage == ReverseMortgage) {
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
