package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.VAGuaranteed
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q625Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q625

  property("Loan amount must be valid for VA guaranteed loans") {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q625.amount")
    val units = config.getInt("edits.Q625.units")

    forAll(larGen) { lar =>
      whenever(
        lar.loan.loanType != VAGuaranteed || lar.property.totalUnits > units) {
        lar.mustPass
      }

      val appLar = lar.copy(loan = lar.loan.copy(loanType = VAGuaranteed),
                            property = lar.property.copy(totalUnits = 1))
      appLar.copy(loan = appLar.loan.copy(amount = amount - 1)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount + 1)).mustFail
    }
  }
}
