package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V625_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V625-1"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.geography.tract is equalTo("NA") or (lar.geography.tract.size is equalTo(
      11))
  }
}
