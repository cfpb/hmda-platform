package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  InvalidManufacturedHomeSecuredPropertyCode,
  ManufacturedHomeSecuredNotApplicable
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V689_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V689_2

  property(
    "If multifamily units is a number, Manufactured Home Secured Property Type must be NA or Exempt") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        property = lar.property.copy(multiFamilyAffordableUnits = "NA"))
      unappLar.mustPass

      val appLar =
        lar.copy(property = lar.property.copy(multiFamilyAffordableUnits = "6"))
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeSecuredProperty =
            new InvalidManufacturedHomeSecuredPropertyCode))
        .mustFail
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeSecuredProperty =
            ManufacturedHomeSecuredNotApplicable))
        .mustPass
    }
  }
}
