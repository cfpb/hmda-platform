package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidManufacturedHomeLandPropertyCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V690_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V690_1

  property("Manufactured Home Land Property Interest must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar = lar.copy(
        property = lar.property.copy(manufacturedHomeLandPropertyInterest =
          new InvalidManufacturedHomeLandPropertyCode))
      invalidLar.mustFail
    }
  }
}
