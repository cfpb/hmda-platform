package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V650 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V650"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.sex.sexEnum is equalTo(SexNoCoApplicant)) {
      lar.coApplicant.sex.sexObservedEnum is equalTo(SexObservedNoCoApplicant)
    } and when(lar.coApplicant.sex.sexObservedEnum is equalTo(SexObservedNoCoApplicant)) {
      lar.coApplicant.sex.sexEnum is equalTo(SexNoCoApplicant)
    }
}
