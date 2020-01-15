package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V658_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V658_2

  property("HOEPA status must be valid for the action taken type") {
    forAll(larGen) { lar =>
      val unapplicableLar = lar.copy(
        action = lar.action.copy(actionTakenType = new InvalidActionTakenTypeCode))
      unapplicableLar.mustPass

      val applicableLar =
        lar.copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
      val invalidLar = applicableLar.copy(hoepaStatus = HighCostMortgage)
      val validLar = applicableLar.copy(hoepaStatus = HOEPStatusANotApplicable)

      invalidLar.mustFail
      validLar.mustPass
    }
  }
}
