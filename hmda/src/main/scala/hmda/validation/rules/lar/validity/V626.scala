package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V626 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V626"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.geography.county.toLowerCase is equalTo("na") or (lar.geography.county.size is equalTo(
      5))
  }
}
