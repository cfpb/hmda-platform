package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ApplicationApprovedButNotAccepted,
  LoanOriginated
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V672_6Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V672_6

  property("If application not accepted, loan costs must be NA") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      unappLar.mustPass

      val appLar = lar.copy(
        action =
          lar.action.copy(actionTakenType = ApplicationApprovedButNotAccepted))
      appLar
        .copy(
          loanDisclosure = appLar.loanDisclosure.copy(totalLoanCosts = "-10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure = appLar.loanDisclosure.copy(totalLoanCosts = "10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure = appLar.loanDisclosure.copy(totalLoanCosts = "NA"))
        .mustPass
    }
  }
}
