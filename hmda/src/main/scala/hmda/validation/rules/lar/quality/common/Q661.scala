package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon.{ lessThan, lessThanOrEqual, when }
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q661 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q661"

  protected val units: Int
  protected val amount: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.totalUnits is lessThanOrEqual(units)) {
      lar.loan.amount is lessThan(amount)
    }
}
