package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V676_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V676_2

  property("If loan is a reverse mortgage, lender credits must be NA") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage != ReverseMortgage) {
        lar.mustPass
      }

      val appLar = lar.copy(reverseMortgage = ReverseMortgage)
      appLar
        .copy(
          loanDisclosure = appLar.loanDisclosure.copy(lenderCredits = "-10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure = appLar.loanDisclosure.copy(lenderCredits = "10.0"))
        .mustFail
      appLar
        .copy(loanDisclosure = appLar.loanDisclosure.copy(lenderCredits = "NA"))
        .mustPass
    }
  }
}
