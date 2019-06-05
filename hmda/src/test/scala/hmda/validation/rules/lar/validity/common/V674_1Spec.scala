package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V674_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V674_1

  property("Origination charges must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(originationCharges = "test"))
        .mustFail
      lar
        .copy(loanDisclosure =
          lar.loanDisclosure.copy(originationCharges = "-10.0"))
        .mustFail
    }
  }
}
