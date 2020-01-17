package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidLineOfCreditCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V707Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V707

  property("Line of credit must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar
        .copy(lineOfCredit = new InvalidLineOfCreditCode)
        .mustFail
    }
  }
}
