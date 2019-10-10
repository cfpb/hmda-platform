package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.HomePurchase
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q628Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q628

  property("Loan amount must be valid for home purchase loans") {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q628.amount")

    forAll(larGen) { lar =>
      whenever(
        lar.loan.loanPurpose != HomePurchase || lar.property.totalUnits > 4) {
        lar.mustPass
      }

      val appLar = lar.copy(loan = lar.loan.copy(loanPurpose = HomePurchase),
                            property = lar.property.copy(totalUnits = 1))
      appLar.copy(loan = appLar.loan.copy(amount = amount + 1)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount)).mustFail
      appLar.copy(loan = appLar.loan.copy(amount = amount - 1)).mustFail
    }
  }
}
