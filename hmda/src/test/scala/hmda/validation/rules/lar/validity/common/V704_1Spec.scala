package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V704_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V704_1
  property("If loan is purchased, AUS must be NA or Exempt") {
    forAll(larGen) { lar =>
      whenever(lar.action.actionTakenType != PurchasedLoan) {
        lar.mustPass
      }

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))

      appLar.copy(AUS = appLar.AUS.copy(aus1 = EmptyAUSValue)).mustFail
      appLar.copy(AUS = appLar.AUS.copy(aus1 = AUSNotApplicable)).mustPass
      appLar.copy(AUS = appLar.AUS.copy(aus1 = AUSExempt)).mustPass
    }
  }
}
