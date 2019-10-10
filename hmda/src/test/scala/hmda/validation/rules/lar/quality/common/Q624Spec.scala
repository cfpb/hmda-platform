package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.FHAInsured
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q624Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q624

  property("Loan amount must be valid for single unit FHA loans") {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q624.amount")

    forAll(larGen) { lar =>
      whenever(lar.loan.loanType != FHAInsured || lar.property.totalUnits != 1) {
        lar.mustPass
      }

      val appLar = lar.copy(loan = lar.loan.copy(loanType = FHAInsured),
                            property = lar.property.copy(totalUnits = 1))
      appLar.copy(loan = appLar.loan.copy(amount = amount - 1)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount + 1)).mustFail
    }
  }
}
