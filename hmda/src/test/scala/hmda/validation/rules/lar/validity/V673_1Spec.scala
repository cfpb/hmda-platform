package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V673_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V673_1

  property("Total points and fee must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "test"))
        .mustFail
      lar
        .copy(loanDisclosure =
          lar.loanDisclosure.copy(totalPointsAndFees = "-10.0"))
        .mustFail
    }
  }
}
