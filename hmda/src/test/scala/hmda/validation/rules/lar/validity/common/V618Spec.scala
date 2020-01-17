package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidActionTakenTypeCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V618Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V618

  property("Action taken must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val badAction =
        lar.action.copy(actionTakenType = new InvalidActionTakenTypeCode)
      lar.copy(action = badAction).mustFail
    }
  }
}
