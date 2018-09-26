package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V673_5Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V673_5

  property("Total points and fees must be NA if there are loan costs") {
    forAll(larGen) { lar =>
      whenever(lar.loanDisclosure.totalLoanCosts == "NA") {
        lar.mustPass
      }

      val appLar =
        lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "0"))
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(totalPointsAndFees = "-10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(totalPointsAndFees = "10.0"))
        .mustFail
      appLar
        .copy(loanDisclosure =
          appLar.loanDisclosure.copy(totalPointsAndFees = "NA"))
        .mustPass
    }
  }
}
