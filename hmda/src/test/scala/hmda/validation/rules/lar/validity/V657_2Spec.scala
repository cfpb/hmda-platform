package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ApplicationDenied, LoanOriginated}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V657_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V657_2

  property("Rate spread must be NA or exempt") {
    forAll(larGen) { lar =>
      val wrongAction =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      wrongAction.mustPass

      val rightAction =
        lar.copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
      rightAction.copy(loan = rightAction.loan.copy(rateSpread = "NA")).mustPass
      rightAction
        .copy(loan = rightAction.loan.copy(rateSpread = "Exempt"))
        .mustPass
      rightAction
        .copy(loan = rightAction.loan.copy(rateSpread = "test"))
        .mustFail
    }
  }
}
