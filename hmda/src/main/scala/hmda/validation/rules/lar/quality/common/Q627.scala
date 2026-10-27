package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q627"
  protected val minAmount: Int
  protected val maxAmount: Int
  protected val units: Int
  protected val isRangeExclusive: Boolean = false

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.totalUnits is greaterThanOrEqual(units)) {
      if (isRangeExclusive) {
        lar.loan.amount is lessThan(maxAmount) and
          (lar.loan.amount is greaterThan(minAmount))
      } else {
        lar.loan.amount is lessThanOrEqual(maxAmount) and
          (lar.loan.amount is greaterThanOrEqual(minAmount))
      }
    }
}

object Q627 extends Q627 {
  override protected val minAmount: Int = 100000
  override protected val maxAmount: Int = 10000000
  override protected val units: Int = 5
}
