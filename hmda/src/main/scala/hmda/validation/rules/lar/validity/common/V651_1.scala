package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V651_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V651-1"

  override def parent: String = "V651"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicant.age is greaterThan(0)
}
