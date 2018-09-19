package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V670_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V670_4

  property("If no denial reason is provided, application should not be denied") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(denial = lar.denial.copy(denialReason1 = DebtToIncomeRatio))
      unappLar.mustPass

      val appLar = lar.copy(
        denial = lar.denial.copy(denialReason1 = DenialReasonNotApplicable))
      appLar
        .copy(action = appLar.action.copy(actionTakenType = ApplicationDenied))
        .mustFail
      appLar
        .copy(
          action = appLar.action.copy(
            actionTakenType = ApplicationApprovedButNotAccepted))
        .mustPass
    }
  }
}
