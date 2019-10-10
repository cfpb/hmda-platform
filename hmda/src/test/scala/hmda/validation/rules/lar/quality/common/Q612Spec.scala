package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{FannieMae, FreddieMac, HighCostMortgage, NotHighCostMortgage}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q612Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q612

  property(
    "If purchaser if Freddie or Fannie, HOEPA status must not be high cost") {
    forAll(larGen) { lar =>
      whenever(
        lar.purchaserType != FannieMae && lar.purchaserType != FreddieMac) {
        lar.mustPass
      }

      val appLar = lar.copy(purchaserType = FannieMae)
      appLar.copy(hoepaStatus = NotHighCostMortgage).mustPass
      appLar.copy(hoepaStatus = HighCostMortgage).mustFail
    }
  }
}
