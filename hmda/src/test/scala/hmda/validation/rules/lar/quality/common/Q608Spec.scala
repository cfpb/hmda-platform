package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.LoanOriginated
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q608Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q608

  property(
    "If loan is originated, action taken should be after application date") {
    forAll(larGen) { lar =>
      whenever(
        lar.loan.applicationDate == "NA" || lar.action.actionTakenType != LoanOriginated) {
        lar.mustPass
      }
      val validAppDate =
        lar.copy(loan = lar.loan.copy(applicationDate = "20000101"),
                 action = lar.action.copy(actionTakenType = LoanOriginated))
      validAppDate.mustPass

      val invalidAction = validAppDate.copy(
        action = validAppDate.action.copy(actionTakenDate = 17760704))
      invalidAction.mustFail
    }
  }
}
