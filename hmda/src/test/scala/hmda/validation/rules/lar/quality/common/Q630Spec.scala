package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
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
      val config = ConfigFactory.load()
      val units = config.getInt("edits.Q630.units")

      lar.copy(property = lar.property.copy(totalUnits = units - 1)).mustPass

      val appLar =
        lar.copy(property = lar.property.copy(totalUnits = units))
      appLar.copy(hoepaStatus = HighCostMortgage).mustFail
      appLar.copy(hoepaStatus = HOEPStatusANotApplicable).mustPass
    }
  }
}
