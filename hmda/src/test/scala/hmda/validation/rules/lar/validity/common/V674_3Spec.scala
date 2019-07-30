package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.OpenEndLineOfCredit
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V674_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V674_3

  property(
    "If loan is an open-end line of credit, origination charges must be NA") {
    forAll(larGen) { lar =>
      whenever(lar.lineOfCredit != OpenEndLineOfCredit) {
        lar.mustPass
      }

      val appLar = lar.copy(lineOfCredit = OpenEndLineOfCredit)
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(originationCharges = "-10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(originationCharges = "10.0"))
        .mustFail
      appLar
        .copy(loanDisclosure =
          appLar.loanDisclosure.copy(originationCharges = "NA"))
        .mustPass
    }
  }
}
