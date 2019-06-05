package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V704_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V704_2
  property("If no other AUS reported Lar must have at least one AUS as Other") {
    forAll(larGen) { lar =>
      whenever(lar.action.actionTakenType != PurchasedLoan) {
        lar.mustPass
      }

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(ausResult1 = EmptyAUSResultValue))
        .mustFail
      appLar
        .copy(
          ausResult = appLar.ausResult.copy(
            ausResult1 = AutomatedUnderwritingResultNotApplicable))
        .mustPass
    }
  }
}
