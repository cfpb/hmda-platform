package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V709Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V709

  property(
    "When property address is exempt, all fields must be reported Exempt") {
    forAll(larGen) { lar =>
      lar.mustPass

      val invalidLar1 =
        lar.copy(geography = lar.geography.copy(street = "Exempt"))
      invalidLar1.mustFail
      val invalidLar2 =
        lar.copy(geography = lar.geography.copy(zipCode = "Exempt"))
      invalidLar2.mustFail

      val validLar = lar.copy(
        geography = lar.geography
          .copy(street = "Exempt", zipCode = "Exempt", city = "Exempt"))
      validLar.mustPass
    }
  }
}
