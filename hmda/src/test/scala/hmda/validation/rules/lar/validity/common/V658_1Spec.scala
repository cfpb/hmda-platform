package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidHoepaStatusCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V658_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V658_1

  property("HOEPA status must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(hoepaStatus = new InvalidHoepaStatusCode)
      invalidLar.mustFail
    }
  }
}
