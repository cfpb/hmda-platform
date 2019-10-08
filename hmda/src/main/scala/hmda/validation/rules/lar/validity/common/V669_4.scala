package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ DenialReasonNotApplicable, EmptyDenialValue, ExemptDenialReason }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V669_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V669-4"

  override def parent: String = "V669"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.denial.denialReason1 is oneOf(DenialReasonNotApplicable, ExemptDenialReason)) {
      lar.denial.denialReason2 is equalTo(EmptyDenialValue) and
        (lar.denial.denialReason3 is equalTo(EmptyDenialValue)) and
        (lar.denial.denialReason4 is equalTo(EmptyDenialValue))
    }
}
