package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V608_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V608_2

  property("NULI must be valid") {
    forAll(larGen) { lar =>
      lar
        .copy(loan = lar.loan.copy(ULI = "abcdefghijklmnopqrstuvwxyz"))
        .mustPass
      lar.copy(loan = lar.loan.copy(ULI = "abcdefg")).mustPass

      lar.copy(loan = lar.loan.copy(ULI = "wrong format")).mustFail
      lar.copy(loan = lar.loan.copy(ULI = "")).mustFail
      lar.copy(loan = lar.loan.copy(ULI = "wrong.format")).mustFail
      lar.copy(loan = lar.loan.copy(ULI = "Exempt")).mustFail
      lar.copy(loan = lar.loan.copy(ULI = "NA")).mustFail
      lar.copy(loan = lar.loan.copy(ULI = "1111")).mustFail

    }
  }
}
