package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V672_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V672_1

  property("Total loan costs must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "test"))
        .mustFail
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "-10.0"))
        .mustFail
    }
  }
}
