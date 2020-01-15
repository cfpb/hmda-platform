package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidPayableToInstitutionCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V694_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V694_1

  property("Initially payable must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar =
        lar.copy(payableToInstitution = new InvalidPayableToInstitutionCode)
      invalidLar.mustFail
    }
  }
}
