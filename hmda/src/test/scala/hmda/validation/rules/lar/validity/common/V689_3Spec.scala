package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  InvalidManufacturedHomeSecuredPropertyCode,
  ManufacturedHome,
  ManufacturedHomeSecuredNotApplicable,
  SiteBuilt
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V689_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V689_3

  property(
    "If construction method is Site Built, Manufactured Home Secured Property Type must be NA or Exempt") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(loan = lar.loan.copy(constructionMethod = ManufacturedHome))
      unappLar.mustPass

      val appLar =
        lar.copy(loan = lar.loan.copy(constructionMethod = SiteBuilt))
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
