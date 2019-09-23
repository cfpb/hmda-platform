package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.CreditScoreExempt
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V710 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V710"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.applicant.creditScore is equalTo(1111) or
        (lar.applicant.creditScoreType is equalTo(CreditScoreExempt)) or
        (lar.coApplicant.creditScore is equalTo(1111)) or
        (lar.coApplicant.creditScoreType is equalTo(CreditScoreExempt))
    ) {

      lar.applicant.creditScore is equalTo(1111) and
        (lar.applicant.creditScoreType is equalTo(CreditScoreExempt)) and
        (lar.coApplicant.creditScore is equalTo(1111)) and
        (lar.coApplicant.creditScoreType is equalTo(CreditScoreExempt)) and
        (lar.applicant.otherCreditScoreModel is empty) and
        (lar.coApplicant.otherCreditScoreModel is empty)
    }
}
