package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q605_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q605_2

  property("Loan type should fit the purchaser type") {
    forAll(larGen) { lar =>
      whenever(lar.purchaserType != GinnieMae) {
        lar.mustPass
      }

      val appLar = lar.copy(purchaserType = GinnieMae)
      appLar.copy(loan = appLar.loan.copy(loanType = FHAInsured)).mustPass
      appLar.copy(loan = appLar.loan.copy(loanType = Conventional)).mustFail
    }
  }
}
