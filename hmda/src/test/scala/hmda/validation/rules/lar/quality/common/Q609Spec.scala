package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{Bank, FannieMae}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q609Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q609

  property("Rate spread should be valid for the type of purchaser") {
    val config = ConfigFactory.load()
    val rs = config.getDouble("edits.Q609.rateSpread")
    val highRs = rs + 1.0
    val lowRs = rs - 1.0

    forAll(larGen) { lar =>
      lar.copy(purchaserType = Bank).mustPass

      val appLar = lar.copy(purchaserType = FannieMae)
      appLar.copy(loan = appLar.loan.copy(rateSpread = "NA")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = "Exempt")).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = lowRs.toString)).mustPass
      appLar.copy(loan = appLar.loan.copy(rateSpread = rs.toString)).mustPass
      appLar
        .copy(loan = appLar.loan.copy(rateSpread = highRs.toString))
        .mustFail
      appLar.copy(loan = appLar.loan.copy(rateSpread = "abc")).mustFail
    }
  }
}
