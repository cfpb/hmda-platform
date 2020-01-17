package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{InvalidManufacturedHomeSecuredPropertyCode, ManufacturedHome, ManufacturedHomeSecuredNotApplicable}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q618Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q618

  property(
    "If construction method is manufactured, property type should be valid") {
    forAll(larGen) { lar =>
      whenever(lar.loan.constructionMethod != ManufacturedHome) {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan = lar.loan.copy(constructionMethod = ManufacturedHome))
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeSecuredProperty =
            ManufacturedHomeSecuredNotApplicable))
        .mustFail
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeSecuredProperty =
            new InvalidManufacturedHomeSecuredPropertyCode))
        .mustPass
    }
  }
}
