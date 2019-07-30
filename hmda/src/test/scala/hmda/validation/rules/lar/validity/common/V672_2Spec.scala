package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V672_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V672_2

  property("Total loan costs must be NA if there are points and fees") {
    forAll(larGen) { lar =>
      whenever(lar.loanDisclosure.totalPointsAndFees == "NA") {
        lar.mustPass
      }

      val appLar = lar.copy(
        loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "0"))
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
