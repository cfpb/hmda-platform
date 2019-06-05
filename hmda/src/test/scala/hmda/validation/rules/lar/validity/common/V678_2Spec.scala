package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{LoanOriginated, PurchasedLoan}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V678_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V678_2

  property("If loan is purchased, prepayment penalty term must be NA or exempt") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      unappLar.mustPass

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "-10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "NA"))
        .mustPass
    }
  }
}
