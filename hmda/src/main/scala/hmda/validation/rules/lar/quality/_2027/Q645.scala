package hmda.validation.rules.lar.quality._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q645 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q645"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.loan.amount is greaterThanOrEqual(500)
}
