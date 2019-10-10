package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q631Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q631

  property(
    "If loan type is not conventional, total units should be less than or equal to 4") {
    forAll(larGen) { lar =>
      val config = ConfigFactory.load()
      val units = config.getInt("edits.Q631.units")

      whenever(lar.loan.loanType == Conventional) {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan = lar.loan.copy(loanType = FHAInsured))
      appLar
        .copy(property = appLar.property.copy(totalUnits = units + 1))
        .mustFail
      appLar
        .copy(property = appLar.property.copy(totalUnits = units))
        .mustPass
      appLar
        .copy(property = appLar.property.copy(totalUnits = units - 1))
        .mustPass
    }
  }
}
