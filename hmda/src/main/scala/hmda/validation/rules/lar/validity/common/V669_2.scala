package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ DenialReasonNotApplicable, ExemptDenialReason, InvalidDenialReasonCode }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V669_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V669-2"

  override def parent: String = "V669"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.denial.denialReason2 not oneOf(DenialReasonNotApplicable, ExemptDenialReason, new InvalidDenialReasonCode) and
      (lar.denial.denialReason3 not oneOf(DenialReasonNotApplicable, ExemptDenialReason, new InvalidDenialReasonCode)) and
      (lar.denial.denialReason4 not oneOf(DenialReasonNotApplicable, ExemptDenialReason, new InvalidDenialReasonCode))
}
