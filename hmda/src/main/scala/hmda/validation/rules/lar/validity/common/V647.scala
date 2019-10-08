package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V647 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V647"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.sex.sexObservedEnum is equalTo(VisualOrSurnameSex)) {
      lar.coApplicant.sex.sexEnum is oneOf(Male, Female)
    }
}
