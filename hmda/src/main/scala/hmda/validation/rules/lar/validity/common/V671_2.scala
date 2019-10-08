package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.OtherDenialReason
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V671_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V671-2"

  override def parent: String = "V671"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.denial.otherDenialReason not empty
    ) {
      (lar.denial.denialReason1 is equalTo(OtherDenialReason)) or
        (lar.denial.denialReason2 is equalTo(OtherDenialReason)) or
        (lar.denial.denialReason3 is equalTo(OtherDenialReason)) or
        (lar.denial.denialReason4 is equalTo(OtherDenialReason))
    }
}
