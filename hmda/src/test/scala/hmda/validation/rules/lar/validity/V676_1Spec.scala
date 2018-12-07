package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V676_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V676_1

  property("Lender credits must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(lenderCredits = ""))
        .mustPass

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(lenderCredits = "0"))
        .mustFail

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(lenderCredits = "test"))
        .mustFail
      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(lenderCredits = "-10.0"))
        .mustFail
    }
  }
}
