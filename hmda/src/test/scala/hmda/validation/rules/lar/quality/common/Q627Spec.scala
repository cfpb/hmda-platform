package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q627Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q627

  property(
    "Loan amount must be valid for loans with a large number of total units") {
    val config = ConfigFactory.load()
    val minAmount = config.getInt("edits.Q627.minAmount")
    val maxAmount = config.getInt("edits.Q627.maxAmount")
    val units = config.getInt("edits.Q627.units")

    forAll(larGen) { lar =>
      val appLar = lar.copy(property = lar.property.copy(totalUnits = units))
      appLar.copy(loan = appLar.loan.copy(amount = maxAmount)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = minAmount)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = maxAmount + 1)).mustFail
      appLar.copy(loan = appLar.loan.copy(amount = minAmount - 1)).mustFail
    }
  }
}
