package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q630Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q630

  property(
    "If total units is greater than or equal to 5, hoepa status must be not applicable") {
    forAll(larGen) { lar =>
      lar.copy(property = lar.property.copy(totalUnits = 4)).mustPass

      val appLar =
        lar.copy(property = lar.property.copy(totalUnits = 5))
      appLar.copy(hoepaStatus = HighCostMortgage).mustFail
      appLar.copy(hoepaStatus = HOEPStatusANotApplicable).mustPass
    }
  }
}
