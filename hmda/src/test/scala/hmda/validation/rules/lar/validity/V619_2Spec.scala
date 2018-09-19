package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V619_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V619_2

  property("Action taken date must be in the current year") {
    forAll(larGen) { lar =>
      val badActionDate1 =
        lar.action.copy(actionTakenDate = 0)
      val badActionDate2 = lar.action.copy(actionTakenDate = 2017110)
      lar.copy(action = badActionDate1).mustFail
      lar.copy(action = badActionDate2).mustFail

      val goodAction = lar.action.copy(actionTakenDate = 20180101)
      lar.copy(action = goodAction).mustPass
    }
  }
}
