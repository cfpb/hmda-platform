package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V654_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V654_2

  property("Income must be NA if multifamily units is a number") {
    forAll(larGen) { lar =>
      val unapplicableLar = lar.copy(
        property = lar.property.copy(multiFamilyAffordableUnits = "NA"))
      unapplicableLar.mustPass

      val invalidLar =
        lar.copy(income = "1",
                 property = lar.property.copy(multiFamilyAffordableUnits = "1"))
      invalidLar.mustFail

      val validLar =
        lar.copy(income = "NA",
                 property = lar.property.copy(multiFamilyAffordableUnits = "1"))
      validLar.mustPass
    }
  }
}
