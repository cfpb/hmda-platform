package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V672_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V672_3

  property("If loan is a reverse mortgage, loan costs must be NA") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage != ReverseMortgage) {
        lar.mustPass
      }

      val appLar = lar.copy(reverseMortgage = ReverseMortgage)
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
