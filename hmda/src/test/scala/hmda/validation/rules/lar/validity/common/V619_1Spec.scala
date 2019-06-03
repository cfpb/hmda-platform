package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V619_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V619_1

  property("Action taken date must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      val badActionDate1 =
        lar.action.copy(actionTakenDate = 0)
      val badActionDate2 = lar.action.copy(actionTakenDate = 2018110)
      lar.copy(action = badActionDate1).mustFail
      lar.copy(action = badActionDate2).mustFail
    }
  }
}
