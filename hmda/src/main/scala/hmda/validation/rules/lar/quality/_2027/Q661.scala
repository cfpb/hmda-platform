package hmda.validation.rules.lar.quality._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon.{ lessThan, lessThanOrEqual, when }
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q661 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q661"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.totalUnits is lessThanOrEqual(4)) {
      lar.loan.amount is lessThan(50000000)
    }
}
