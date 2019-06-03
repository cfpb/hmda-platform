package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V692_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V692_1

  property("Multifamily affordable units must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(
        property = lar.property.copy(multiFamilyAffordableUnits = "test"))
      invalidLar.mustFail
    }
  }
}
