package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidManufacturedHomeSecuredPropertyCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V689_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V689_1

  property("Manufactured Home Secured Property Type must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(
        property = lar.property.copy(manufacturedHomeSecuredProperty =
          new InvalidManufacturedHomeSecuredPropertyCode))
      invalidLar.mustFail
    }
  }
}
