package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.lar.enums.{Conventional, FHAInsured, FannieMae, FreddieMac}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.quality.common.Q605_1

class Q605_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q605_1

  property("Loan type should fit the purchaser type") {
    forAll(larGen) { lar =>
      whenever(
        lar.purchaserType != FannieMae && lar.purchaserType != FreddieMac) {
        lar.mustPass
      }

      val appLar = lar.copy(purchaserType = FannieMae)
      appLar.copy(loan = appLar.loan.copy(loanType = Conventional)).mustPass
      appLar.copy(loan = appLar.loan.copy(loanType = FHAInsured)).mustFail
    }
  }
}
