package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidMortgageTypeCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V706Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V706

  property("Reverse mortgage must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar
        .copy(reverseMortgage = new InvalidMortgageTypeCode)
        .mustFail
    }
  }
}
