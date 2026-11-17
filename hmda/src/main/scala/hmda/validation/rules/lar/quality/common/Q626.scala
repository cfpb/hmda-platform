package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q626 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q626"
  protected val amount: Int
  protected val units: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.purchaserType is oneOf(FannieMae, GinnieMae, FreddieMac, FarmerMac) and
        (lar.property.totalUnits is lessThanOrEqual(units))
    ) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
}

object Q626 extends Q626 {
  override protected val amount: Int = 1225000
  override protected val units: Int = 4
}
