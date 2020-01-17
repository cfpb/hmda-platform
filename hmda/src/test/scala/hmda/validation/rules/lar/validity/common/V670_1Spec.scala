package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V670_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V670_1

  property("If application is denied, a denial reason should be provided") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        action = lar.action.copy(actionTakenType = new InvalidActionTakenTypeCode))
      unappLar.mustPass

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
      appLar
        .copy(denial = appLar.denial.copy(denialReason1 = EmptyDenialValue))
        .mustFail
      appLar
        .copy(denial = appLar.denial.copy(denialReason1 = DebtToIncomeRatio))
        .mustPass
    }
  }
}
