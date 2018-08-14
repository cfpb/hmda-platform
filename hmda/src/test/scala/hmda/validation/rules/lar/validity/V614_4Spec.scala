package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V614_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V614_4

  property("Non-open ended lines of credit must pass") {
    forAll(larGen) { lar =>
      whenever(lar.lineOfCredit == NotOpenEndLineOfCredit) {
        lar.mustPass
      }
    }
  }

  property("Open ended lines of credit must have no preapproval requested") {
    forAll(larGen) { lar =>
      whenever(lar.lineOfCredit == OpenEndLineOfCredit) {
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
