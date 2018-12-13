package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V627"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.geography.county.size is equalTo(5) and (lar.geography.tract
      .substring(0, 5) is equalTo(lar.geography.county))
  }
}
