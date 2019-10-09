package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q626 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q626"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q626.amount")
    val units  = config.getInt("edits.Q626.units")

    when(
      lar.purchaserType is oneOf(FannieMae, GinnieMae, FreddieMac, FarmerMac) and
        (lar.property.totalUnits is lessThanOrEqual(units))
    ) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
  }
}
