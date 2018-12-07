package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V675_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V675_1

  property("Discount points must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(discountPoints = ""))
        .mustPass

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(discountPoints = "0"))
        .mustFail

      lar
        .copy(loanDisclosure = lar.loanDisclosure.copy(discountPoints = "test"))
        .mustFail
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(discountPoints = "-10.0"))
        .mustFail
    }
  }
}
