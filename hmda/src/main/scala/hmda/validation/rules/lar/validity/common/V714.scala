package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ApplicationSubmissionExempt, PayableToInstitutionExempt }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V714 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V714"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.applicationSubmission is equalTo(ApplicationSubmissionExempt) or
        (lar.payableToInstitution is equalTo(PayableToInstitutionExempt))
    ) {
      lar.applicationSubmission is equalTo(ApplicationSubmissionExempt) and
        (lar.payableToInstitution is equalTo(PayableToInstitutionExempt))
    }
}
