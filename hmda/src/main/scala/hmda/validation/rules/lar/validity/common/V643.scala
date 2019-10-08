package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V643 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V643"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.sex.sexObservedEnum is equalTo(VisualOrSurnameSex)) {
      lar.applicant.sex.sexEnum is oneOf(Male, Female)
    }
}
