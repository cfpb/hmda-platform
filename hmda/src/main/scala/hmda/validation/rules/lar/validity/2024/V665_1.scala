package hmda.validation.rules.lar.validity._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V665_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V665-1"

  override def parent: String = "V665"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.creditScore is numeric
}
