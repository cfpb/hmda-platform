package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.InvalidLienStatusCode
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V659Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V659

  property("Lein status must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(lienStatus = new InvalidLienStatusCode)
      invalidLar.mustFail
    }
  }
}
