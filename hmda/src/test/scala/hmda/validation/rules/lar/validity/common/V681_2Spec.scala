package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ApplicationWithdrawnByApplicant,
  LoanOriginated
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V681_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V681_2

  property("Combined loan to value ratio must be NA or exempt") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      unappLar.mustPass

      val appLar = lar.copy(
        action =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant))

      appLar
        .copy(loan = appLar.loan.copy(combinedLoanToValueRatio = "test"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(combinedLoanToValueRatio = "1.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(combinedLoanToValueRatio = "NA"))
        .mustPass
    }
  }
}
