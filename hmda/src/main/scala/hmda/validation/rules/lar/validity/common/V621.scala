package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V621 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V621"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.geography.city not empty
}
