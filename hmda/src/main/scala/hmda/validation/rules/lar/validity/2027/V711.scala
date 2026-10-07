package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ExemptDenialReason
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait V711 extends EditCheck[LoanApplicationRegister] {
  override def parent: String = "V711"

  protected def anyExemption(lar: LoanApplicationRegister)(exemptionsTest: => ValidationResult): ValidationResult =
    when(
      lar.denial.denialReason1 is equalTo(ExemptDenialReason) or
        (lar.denial.denialReason2 is equalTo(ExemptDenialReason)) or
        (lar.denial.denialReason3 is equalTo(ExemptDenialReason)) or
        (lar.denial.denialReason4 is equalTo(ExemptDenialReason))
    )(exemptionsTest)
}