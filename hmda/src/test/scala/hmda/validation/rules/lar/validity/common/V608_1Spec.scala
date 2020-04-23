package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V608_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V608_1

  property("ULI must be valid") {
    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(ULI = "too short")).mustPass
      lar
        .copy(loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyz"))
        .mustPass

      lar
        .copy(
          loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"))
        .mustFail
      lar
        .copy(
          loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyz--abcdefghijklmnopqrstuvwxyz"))
        .mustFail
      lar
        .copy(
          loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyz..abcdefghijklmnopqrstuvwxyz"))
        .mustFail
      lar
        .copy(
          loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyz  abcdefghijklmnopqrstuvwxyz"))
        .mustFail
    }
  }
}
