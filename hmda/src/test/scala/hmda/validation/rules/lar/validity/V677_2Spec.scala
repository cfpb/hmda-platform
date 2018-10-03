package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ApplicationDenied, LoanOriginated}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V677_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V677_2

  property("If loan is not approved, interest rate must be exempt or NA") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      unappLar.mustPass

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
      appLar.copy(loan = appLar.loan.copy(interestRate = "test")).mustFail
      appLar.copy(loan = appLar.loan.copy(interestRate = "1.0")).mustFail
      appLar.copy(loan = appLar.loan.copy(interestRate = "NA")).mustPass
    }
  }
}
