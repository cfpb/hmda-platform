package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.FHAInsured
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q624Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q624

  property("Loan amount must be valid for single unit FHA loans") {
    forAll(larGen) { lar =>
      whenever(lar.loan.loanType != FHAInsured || lar.property.totalUnits != 1) {
        lar.mustPass
      }

      val appLar = lar.copy(loan = lar.loan.copy(loanType = FHAInsured),
                            property = lar.property.copy(totalUnits = 1))
      appLar.copy(loan = appLar.loan.copy(amount = 600000)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = 637000)).mustPass
      appLar.copy(loan = appLar.loan.copy(amount = 637001)).mustFail
    }
  }
}
