package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.InvalidPreapprovalCode

class V613_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V613_1

  property("Preapproval must equal 1 or 2, and cannot be left blank.") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  property("Preapproval must fail if not equal to 1 or 2") {
    forAll(larGen) { lar =>
      lar
        .copy(action = lar.action.copy(preapproval = new InvalidPreapprovalCode))
        .mustFail
    }
  }
}
