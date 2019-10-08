package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q606 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q606"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.income is numeric) {
      lar.income.toInt is lessThan(3000)
    }
}
