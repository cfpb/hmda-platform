package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.FannieMae
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q626Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q626

  property("Loan amount must be valid for purchased loans with 4 or less units") {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q626.amount")
    val units = config.getInt("edits.Q626.units")

    forAll(larGen) { lar =>
      whenever(lar.property.totalUnits > units) {
        lar.mustPass
      }

      val appLar = lar.copy(purchaserType = FannieMae,
                            property = lar.property.copy(totalUnits = units))
      appLar.copy(loan = appLar.loan.copy(amount = amount - 1)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount + 1)).mustFail
    }
  }
}
