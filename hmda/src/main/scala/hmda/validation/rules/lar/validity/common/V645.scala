package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V645 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V645"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.sex.sexEnum is equalTo(SexNotApplicable)) {
      lar.applicant.sex.sexObservedEnum is equalTo(SexObservedNotApplicable)
    }
}
