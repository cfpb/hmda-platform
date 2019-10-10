package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{DirectOwnership, ManufacturedHome, ManufacturedHomeLandNotApplicable}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q619Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q619

  property(
    "If construction method is manufactured, property interest should not be NA") {
    forAll(larGen) { lar =>
      whenever(lar.loan.constructionMethod != ManufacturedHome) {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan = lar.loan.copy(constructionMethod = ManufacturedHome))
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeLandPropertyInterest =
            ManufacturedHomeLandNotApplicable))
        .mustFail
      appLar
        .copy(
          property = appLar.property.copy(
            manufacturedHomeLandPropertyInterest = DirectOwnership))
        .mustPass
    }
  }
}
