package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V652_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V652-1"

  override def parent: String = "V652"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.age is greaterThan(0)
}
