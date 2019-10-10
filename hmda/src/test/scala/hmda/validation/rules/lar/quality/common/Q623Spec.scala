package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q623Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q623

  property("Loan amount should be realistic for the income and total units") {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q623.amount")
    val larIncome = config.getInt("edits.Q623.income")

    forAll(larGen) { lar =>
      whenever(lar.income == "NA" || lar.property.totalUnits > 5) {
        lar.mustPass
      }

      val appLar = lar.copy(income = larIncome.toString,
                            property = lar.property.copy(totalUnits = 1))
      appLar.copy(loan = appLar.loan.copy(amount = amount - 1)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = amount)).mustFail
      appLar.copy(loan = appLar.loan.copy(amount = amount + 1)).mustFail
    }
  }
}
