package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V670_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V670_3

  property("If application is not denied, no denial reason should be provided") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        action = lar.action.copy(actionTakenType = new InvalidActionTakenTypeCode))
      unappLar.mustPass

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      appLar
        .copy(denial = appLar.denial.copy(denialReason1 = DebtToIncomeRatio))
        .mustFail
      appLar
        .copy(denial =
          appLar.denial.copy(denialReason1 = DenialReasonNotApplicable))
        .mustPass
    }
  }
}
