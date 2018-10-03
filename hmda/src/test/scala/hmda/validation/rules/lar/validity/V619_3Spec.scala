package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{LoanOriginated, PurchasedLoan}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V619_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V619_3

  property("Action taken date must be after application date") {
    forAll(larGen) { lar =>
      whenever(lar.loan.applicationDate == "NA") {
        lar.mustPass
      }
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))
      unappLar.mustPass

      val appLar = lar.copy(loan = lar.loan.copy(applicationDate = "20180606"),
                            action =
                              lar.action.copy(actionTakenType = LoanOriginated))
      appLar
        .copy(action = appLar.action.copy(actionTakenDate = 20180606))
        .mustPass
      appLar
        .copy(action = appLar.action.copy(actionTakenDate = 20180607))
        .mustPass
      appLar
        .copy(action = appLar.action.copy(actionTakenDate = 20180506))
        .mustFail
      appLar.copy(action = appLar.action.copy(actionTakenDate = 12)).mustFail
    }
  }
}
