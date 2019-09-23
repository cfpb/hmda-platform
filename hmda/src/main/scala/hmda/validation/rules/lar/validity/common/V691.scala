package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V691 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V691"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.property.totalUnits is greaterThan(0)
}
