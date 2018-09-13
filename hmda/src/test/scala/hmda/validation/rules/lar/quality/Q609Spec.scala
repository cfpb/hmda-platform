package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{Bank, FannieMae}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q609Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q609

  property("Rate spread should be valid for the type of purchaser") {
    forAll(larGen) { lar =>
      lar.copy(purchaserType = Bank).mustPass

      val appLar = lar.copy(purchaserType = FannieMae)
      appLar.copy(loan = appLar.loan.copy(rateSpread = "NA")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = "Exempt")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = "0.1")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = "10.0")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = "10.01")).mustFail
      appLar.copy(loan = appLar.loan.copy(rateSpread = "abc")).mustFail
    }
  }
}
