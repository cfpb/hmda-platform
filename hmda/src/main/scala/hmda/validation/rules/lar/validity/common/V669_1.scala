package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EmptyDenialValue, InvalidDenialReasonCode }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V669_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V669-1"

  override def parent: String = "V669"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.denial.denialReason1 not oneOf(EmptyDenialValue, new InvalidDenialReasonCode)
}
